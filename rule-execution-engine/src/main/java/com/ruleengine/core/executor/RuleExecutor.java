package com.ruleengine.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruleengine.core.action.Action;
import com.ruleengine.core.action.ActionCreationException;
import com.ruleengine.core.action.ActionException;
import com.ruleengine.core.action.ActionRegistry;
import com.ruleengine.core.action.ActionResult;
import com.ruleengine.core.context.ErrorInfo;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.context.ExecutionStep;
import com.ruleengine.core.expression.ExpressionEvaluationException;
import com.ruleengine.core.expression.ExpressionEvaluator;
import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;
import com.ruleengine.core.model.TransitionDefinition;

/**
 * Core rule execution engine.
 * Orchestrates rule execution, action execution, and transition navigation.
 *
 * Enhanced rule execution engine with complete error handling and timeout support.
 */
public class RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);
    
    private final RuleEngineConfig config;
    private final Map<String, RuleDefinition> ruleMap;
    private final ActionRegistry actionRegistry;
    private final ExpressionEvaluator expressionEvaluator;
    private final int maxDepth;
    private final String defaultErrorRule;
    private final long timeoutMs;
    private final ExecutorService executorService;
    
    public RuleExecutor(
            RuleEngineConfig config,
            ActionRegistry actionRegistry,
            ExpressionEvaluator expressionEvaluator) {
        
        this.config = config;
        this.ruleMap = config.buildRuleMap();
        this.actionRegistry = actionRegistry;
        this.expressionEvaluator = expressionEvaluator;
        this.maxDepth = config.getGlobalSettings().getMaxExecutionDepth();
        this.defaultErrorRule = config.getGlobalSettings().getDefaultErrorRule();
        this.timeoutMs = config.getGlobalSettings().getTimeout();
        this.executorService = Executors.newCachedThreadPool();
        
        logger.info("RuleExecutor initialized with {} rules, maxDepth={}, timeout={}ms", 
                   ruleMap.size(), maxDepth, timeoutMs);
    }
    
    /**
     * Execute rules with timeout enforcement.
     */
    public ExecutionResult execute(ExecutionContext context) throws RuleExecutionException {
        long startTime = System.currentTimeMillis();
        
        String entryPoint = config.getEntryPoint();
        if (entryPoint == null || entryPoint.trim().isEmpty()) {
            throw new RuleExecutionException("Entry point is not configured");
        }
        
        logger.info("Starting rule execution from entry point: {}", entryPoint);
        
        // Initialize trace with actual entry point
        if (context.isTracingEnabled()) {
            context.initializeTrace(entryPoint);
            context.getTrace().snapshotVariables("initial-state", context);
        }
        
        // Execute with timeout
        Future<ExecutionResult> future = executorService.submit(() -> {
            try {
                String finalRuleId = executeFromRule(entryPoint, context);
                long executionTime = System.currentTimeMillis() - startTime;
                
                logger.info("Rule execution completed successfully. Final rule: {}, Time: {}ms", 
                           finalRuleId, executionTime);
                
                return ExecutionResult.builder()
                        .success(true)
                        .context(context)
                        .finalRuleId(finalRuleId)
                        .executionTimeMs(executionTime)
                        .build();
                        
            } catch (Exception e) {
                long executionTime = System.currentTimeMillis() - startTime;
                logger.error("Rule execution failed after {}ms: {}", executionTime, e.getMessage(), e);
                
                return ExecutionResult.builder()
                        .success(false)
                        .context(context)
                        .finalRuleId(context.getCurrentRuleId())
                        .errorMessage(e.getMessage())
                        .executionTimeMs(executionTime)
                        .build();
            }
        });
        
        try {
        	ExecutionResult executionResult = future.get(timeoutMs, TimeUnit.MILLISECONDS);
        	
            if (context.isTracingEnabled()) {
                context.getTrace().snapshotVariables("final-state", context);
            }

            return executionResult;
        } catch (TimeoutException e) {
            future.cancel(true);
            long executionTime = System.currentTimeMillis() - startTime;
            
            logger.error("Rule execution timed out after {}ms", executionTime);
            
            if (context.isTracingEnabled()) {
                context.getTrace().snapshotVariables("final-state", context);
            }
            
            return ExecutionResult.builder()
                    .success(false)
                    .context(context)
                    .finalRuleId(context.getCurrentRuleId())
                    .errorMessage("Execution timed out after " + timeoutMs + "ms")
                    .executionTimeMs(executionTime)
                    .build();
                    
        } catch (InterruptedException | ExecutionException e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            if (context.isTracingEnabled()) {
                context.getTrace().snapshotVariables("final-state", context);
            }
            
            return ExecutionResult.builder()
                    .success(false)
                    .context(context)
                    .finalRuleId(context.getCurrentRuleId())
                    .errorMessage("Execution failed: " + e.getMessage())
                    .executionTimeMs(executionTime)
                    .build();
        }
    }
    
    private String executeFromRule(String ruleId, ExecutionContext context) 
            throws RuleExecutionException {
        
        String currentRuleId = ruleId;
        
        while (currentRuleId != null) {
            if (context.getDepth() >= maxDepth) {
                throw new RuleExecutionException(
                    currentRuleId,
                    "Maximum execution depth exceeded: " + maxDepth
                );
            }
            
            RuleDefinition rule = ruleMap.get(currentRuleId);
            if (rule == null) {
                throw new RuleExecutionException(
                    currentRuleId,
                    "Rule not found: " + currentRuleId
                );
            }
            
            try {
                logger.debug("Executing rule: {}", currentRuleId);
                
                context.setCurrentRuleId(currentRuleId);
                context.incrementDepth();
                
                context.addExecutionStep(
                    ExecutionStep.builder(ExecutionStep.StepType.RULE_ENTERED)
                        .ruleId(currentRuleId)
                        .build()
                );
                
                executeActions(rule, context);
                
                context.addExecutionStep(
                    ExecutionStep.builder(ExecutionStep.StepType.RULE_EXITED)
                        .ruleId(currentRuleId)
                        .build()
                );
                
                if (rule.isTerminal()) {
                    logger.debug("Reached terminal rule: {}", currentRuleId);
                    return currentRuleId;
                }
                
                String nextRuleId = evaluateTransitions(rule, context);
                
                if (nextRuleId == null) {
                    logger.debug("No transition matched, stopping at rule: {}", currentRuleId);
                    return currentRuleId;
                }
                
                currentRuleId = nextRuleId;
                
            } catch (ActionException e) {
                String errorRuleId = handleActionError(rule, e, context);
                if (errorRuleId != null) {
                    currentRuleId = errorRuleId;
                } else {
                    throw new RuleExecutionException(currentRuleId, "Action execution failed", e);
                }
            } catch (Exception e) {
                throw new RuleExecutionException(currentRuleId, "Rule execution failed", e);
            }
        }
        
        return context.getCurrentRuleId();
    }
    
    private void executeActions(RuleDefinition rule, ExecutionContext context) 
            throws ActionException, ActionCreationException, ExpressionEvaluationException {
        
        List<ActionDefinition> actions = rule.getActions();
        if (actions == null || actions.isEmpty()) {
            logger.debug("Rule {} has no actions", rule.getRuleId());
            return;
        }
        
        logger.debug("Executing {} actions for rule: {}", actions.size(), rule.getRuleId());
        
        for (ActionDefinition actionDef : actions) {
            // Check conditional execution
            if (actionDef.hasCondition() && !evaluateActionCondition(actionDef, context)) {
                logger.debug("Skipping action {} - condition not met", actionDef.getActionId());
                continue;
            }
            
            executeAction(actionDef, context);
        }
    }
    
    private boolean evaluateActionCondition(ActionDefinition actionDef, ExecutionContext context) {
        try {
            return expressionEvaluator.evaluateBoolean(actionDef.getCondition(), context);
        } catch (ExpressionEvaluationException e) {
            logger.warn("Failed to evaluate action condition, defaulting to true: {}", 
                       e.getMessage());
            return true;
        }
    }
    
    private void executeAction(ActionDefinition actionDef, ExecutionContext context) 
            throws ActionException, ActionCreationException, ExpressionEvaluationException {
        
        String actionId = actionDef.getActionId();
        long startTime = System.currentTimeMillis();
        
        logger.debug("Executing action: {} (type: {})", actionId, actionDef.getType());
        
        context.addExecutionStep(
            ExecutionStep.builder(ExecutionStep.StepType.ACTION_STARTED)
                .ruleId(context.getCurrentRuleId())
                .actionId(actionId)
                .build()
        );
        
        try {
            Action action = actionRegistry.createAction(actionDef);
            ActionResult result = action.execute(context);
            
            long duration = System.currentTimeMillis() - startTime;
            
            context.addExecutionStep(
                ExecutionStep.builder(ExecutionStep.StepType.ACTION_COMPLETED)
                    .ruleId(context.getCurrentRuleId())
                    .actionId(actionId)
                    .durationMs(duration)
                    .metadata("success", result.isSuccess())
                    .build()
            );
            
            if (result.isSuccess() && actionDef.getOutputVariable() != null) {
                Object dataToStore = result.getData();
                
                if (actionDef.hasOutputExpression()) {
                    // Use unique temp variable to avoid pollution
                    String tempVar = "__temp_result_" + System.nanoTime();
                    context.setVariable(tempVar, dataToStore);
                    
                    try {
                        dataToStore = expressionEvaluator.evaluate(
                            actionDef.getOutputExpression().replace("result", tempVar), 
                            context
                        );
                    } finally {
                        context.removeVariable(tempVar);
                    }
                }
                
                context.setVariable(actionDef.getOutputVariable(), dataToStore);
                logger.debug("Stored action result in variable: {}", actionDef.getOutputVariable());
            }
            
            logger.debug("Action {} completed in {}ms", actionId, duration);
            
        } catch (ActionException e) {
            long duration = System.currentTimeMillis() - startTime;
            
            context.addExecutionStep(
                ExecutionStep.builder(ExecutionStep.StepType.ACTION_FAILED)
                    .ruleId(context.getCurrentRuleId())
                    .actionId(actionId)
                    .durationMs(duration)
                    .metadata("error", e.getMessage())
                    .build()
            );
            
            logger.error("Action {} failed after {}ms: {}", actionId, duration, e.getMessage());
            
            if (actionDef.shouldContinueOnError()) {
                logger.debug("Continuing execution despite action error (continueOnError=true)");
                return;
            }
            
            throw e;
        }
    }
    
    private String evaluateTransitions(RuleDefinition rule, ExecutionContext context) 
            throws RuleExecutionException {
        
        List<TransitionDefinition> transitions = rule.getSortedTransitions();
        
        if (transitions == null || transitions.isEmpty()) {
            logger.debug("Rule {} has no transitions", rule.getRuleId());
            return null;
        }
        
        logger.debug("Evaluating {} transitions for rule: {}", transitions.size(), rule.getRuleId());
        
        for (TransitionDefinition transition : transitions) {
            try {
                String condition = transition.getCondition();
                logger.debug("Evaluating transition condition: {}", condition);
                
                boolean conditionMet = expressionEvaluator.evaluateBoolean(condition, context);
                
                context.addExecutionStep(
                    ExecutionStep.builder(ExecutionStep.StepType.TRANSITION_EVALUATED)
                        .ruleId(rule.getRuleId())
                        .metadata("condition", condition)
                        .metadata("result", conditionMet)
                        .metadata("targetRule", transition.getTargetRule())
                        .build()
                );                
                if (conditionMet) {
                    logger.debug("Transition condition matched, moving to rule: {}", 
                               transition.getTargetRule());
                    
                    if (transition.hasContextTransform()) {
                        applyContextTransformation(transition, context);
                    }
                    
                    return transition.getTargetRule();
                }
                
            } catch (ExpressionEvaluationException e) {
                logger.error("Failed to evaluate transition condition: {}", 
                           transition.getCondition(), e);
                throw new RuleExecutionException(
                    rule.getRuleId(),
                    "Failed to evaluate transition condition: " + e.getMessage(),
                    e
                );
            }
        }
        
        logger.debug("No transition conditions matched for rule: {}", rule.getRuleId());
        return null;
    }
    
    private void applyContextTransformation(TransitionDefinition transition, 
                                           ExecutionContext context) {
        
        Map<String, String> transform = transition.getContextTransform();
        logger.debug("Applying context transformation: {}", transform);
        
        for (Map.Entry<String, String> entry : transform.entrySet()) {
            String targetVar = entry.getKey();
            String sourceVar = entry.getValue();
            
            Object value = context.getVariable(sourceVar);
            context.setVariable(targetVar, value);
            
            logger.debug("Transformed context: {} -> {}", sourceVar, targetVar);
        }
    }
    
    /**
     * Enhanced error handler - finds and routes to action-level error handlers.
     */
    private String handleActionError(RuleDefinition rule, ActionException error, 
                                     ExecutionContext context) {
        
        ErrorInfo errorInfo = ErrorInfo.builder()
                .ruleId(rule.getRuleId())
                .actionId(error.getActionId())
                .message(error.getMessage())
                .errorType("ACTION_ERROR")
                .exception(error)
                .build();
        
        context.setError(errorInfo);
        
        context.addExecutionStep(
            ExecutionStep.builder(ExecutionStep.StepType.ERROR_OCCURRED)
                .ruleId(rule.getRuleId())
                .actionId(error.getActionId())
                .metadata("error", error.getMessage())
                .build()
        );
        
        // Find the specific action definition
        ActionDefinition failedAction = findActionDefinition(rule, error.getActionId());
        
        // Check for action-level error handler
        if (failedAction != null && failedAction.hasErrorHandler()) {
            String errorRuleId = failedAction.getOnError().getTargetRule();
            logger.info("Routing to action-level error handler: {}", errorRuleId);
            return errorRuleId;
        }
        
        // Fall back to default error rule
        if (defaultErrorRule != null) {
            logger.info("Routing to default error rule: {}", defaultErrorRule);
            return defaultErrorRule;
        }
        
        // No error handler configured
        return null;
    }
    
    /**
     * Find action definition by ID within a rule.
     */
    private ActionDefinition findActionDefinition(RuleDefinition rule, String actionId) {
        if (rule.getActions() == null || actionId == null) {
            return null;
        }
        
        return rule.getActions().stream()
                .filter(action -> actionId.equals(action.getActionId()))
                .findFirst()
                .orElse(null);
    }
    
    public RuleEngineConfig getConfig() {
        return config;
    }
    
    /**
     * Shutdown executor service gracefully.
     */
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}