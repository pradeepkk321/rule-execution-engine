package com.ruleengine.core.executor;

import com.ruleengine.core.action.*;
import com.ruleengine.core.context.ErrorInfo;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.context.ExecutionStep;
import com.ruleengine.core.expression.ExpressionEvaluationException;
import com.ruleengine.core.expression.ExpressionEvaluator;
import com.ruleengine.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Core rule execution engine.
 * Orchestrates rule execution, action execution, and transition navigation.
 */
public class RuleExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleExecutor.class);
    
    private final RuleEngineConfig config;
    private final Map<String, RuleDefinition> ruleMap;
    private final ActionRegistry actionRegistry;
    private final ExpressionEvaluator expressionEvaluator;
    private final int maxDepth;
    private final String defaultErrorRule;
    
    /**
     * Create a rule executor.
     */
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
        
        logger.info("RuleExecutor initialized with {} rules, maxDepth={}", 
                   ruleMap.size(), maxDepth);
    }
    
    /**
     * Execute rules starting from the entry point.
     */
    public ExecutionResult execute(ExecutionContext context) throws RuleExecutionException {
        long startTime = System.currentTimeMillis();
        
        String entryPoint = config.getEntryPoint();
        if (entryPoint == null || entryPoint.trim().isEmpty()) {
            throw new RuleExecutionException("Entry point is not configured");
        }
        
        logger.info("Starting rule execution from entry point: {}", entryPoint);
        
        try {
            // Execute from entry point
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
    }
    
    /**
     * Execute rules starting from a specific rule.
     */
    private String executeFromRule(String ruleId, ExecutionContext context) 
            throws RuleExecutionException {
        
        String currentRuleId = ruleId;
        
        while (currentRuleId != null) {
            // Check depth limit
            if (context.getDepth() >= maxDepth) {
                throw new RuleExecutionException(
                    currentRuleId,
                    "Maximum execution depth exceeded: " + maxDepth
                );
            }
            
            // Get rule definition
            RuleDefinition rule = ruleMap.get(currentRuleId);
            if (rule == null) {
                throw new RuleExecutionException(
                    currentRuleId,
                    "Rule not found: " + currentRuleId
                );
            }
            
            // Execute the rule
            try {
                logger.debug("Executing rule: {}", currentRuleId);
                
                context.setCurrentRuleId(currentRuleId);
                context.incrementDepth();
                
                // Record rule entry
                context.addExecutionStep(
                    ExecutionStep.builder(ExecutionStep.StepType.RULE_ENTERED)
                        .ruleId(currentRuleId)
                        .build()
                );
                
                // Execute all actions in the rule
                executeActions(rule, context);
                
                // Record rule exit
                context.addExecutionStep(
                    ExecutionStep.builder(ExecutionStep.StepType.RULE_EXITED)
                        .ruleId(currentRuleId)
                        .build()
                );
                
                // Check if terminal rule
                if (rule.isTerminal()) {
                    logger.debug("Reached terminal rule: {}", currentRuleId);
                    return currentRuleId;
                }
                
                // Evaluate transitions to get next rule
                String nextRuleId = evaluateTransitions(rule, context);
                
                if (nextRuleId == null) {
                    logger.debug("No transition matched, stopping at rule: {}", currentRuleId);
                    return currentRuleId;
                }
                
                currentRuleId = nextRuleId;
                
            } catch (ActionException e) {
                // Handle action error
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
    
    /**
     * Execute all actions in a rule sequentially.
     */
    private void executeActions(RuleDefinition rule, ExecutionContext context) 
            throws ActionException, ActionCreationException {
        
        List<ActionDefinition> actions = rule.getActions();
        if (actions == null || actions.isEmpty()) {
            logger.debug("Rule {} has no actions", rule.getRuleId());
            return;
        }
        
        logger.debug("Executing {} actions for rule: {}", actions.size(), rule.getRuleId());
        
        for (ActionDefinition actionDef : actions) {
            executeAction(actionDef, context);
        }
    }
    
    /**
     * Execute a single action.
     */
    private void executeAction(ActionDefinition actionDef, ExecutionContext context) 
            throws ActionException, ActionCreationException {
        
        String actionId = actionDef.getActionId();
        long startTime = System.currentTimeMillis();
        
        logger.debug("Executing action: {} (type: {})", actionId, actionDef.getType());
        
        // Record action start
        context.addExecutionStep(
            ExecutionStep.builder(ExecutionStep.StepType.ACTION_STARTED)
                .ruleId(context.getCurrentRuleId())
                .actionId(actionId)
                .build()
        );
        
        try {
            // Create action
            Action action = actionRegistry.createAction(actionDef);
            
            // Execute action
            ActionResult result = action.execute(context);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Record action completion
            context.addExecutionStep(
                ExecutionStep.builder(ExecutionStep.StepType.ACTION_COMPLETED)
                    .ruleId(context.getCurrentRuleId())
                    .actionId(actionId)
                    .durationMs(duration)
                    .metadata("success", result.isSuccess())
                    .build()
            );
            
            // Store result in context if outputVariable is specified
            if (result.isSuccess() && actionDef.getOutputVariable() != null) {
                Object dataToStore = result.getData();
                
                // If outputExpression is specified, evaluate it to extract partial data
                if (actionDef.hasOutputExpression()) {
                    dataToStore = extractOutputData(actionDef, result.getData(), context);
                }
                
                context.setVariable(actionDef.getOutputVariable(), dataToStore);
                
                logger.debug("Stored action result in variable: {}", actionDef.getOutputVariable());
            }
            
            logger.debug("Action {} completed in {}ms", actionId, duration);
            
        } catch (ActionException e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Record action failure
            context.addExecutionStep(
                ExecutionStep.builder(ExecutionStep.StepType.ACTION_FAILED)
                    .ruleId(context.getCurrentRuleId())
                    .actionId(actionId)
                    .durationMs(duration)
                    .metadata("error", e.getMessage())
                    .build()
            );
            
            logger.error("Action {} failed after {}ms: {}", actionId, duration, e.getMessage());
            
            // Check if we should continue on error
            if (actionDef.shouldContinueOnError()) {
                logger.debug("Continuing execution despite action error (continueOnError=true)");
                return;
            }
            
            throw e;
        }
    }
    
    /**
     * Extract output data using outputExpression.
     * The expression can reference:
     * 1. "result" - the action's result
     * 2. Any other context variables
     */
    private Object extractOutputData(ActionDefinition actionDef, Object fullData, 
                                     ExecutionContext context) throws ActionException {
        
        String expression = actionDef.getOutputExpression();
        
        try {
            // Store action result as "result" for the expression
            context.setVariable("result", fullData);
            
            // Evaluate expression to extract partial data
            Object extracted = expressionEvaluator.evaluate(expression, context);
            
            logger.debug("Extracted output data using expression: {} -> {}", expression, extracted);
            
            return extracted;
            
        } catch (ExpressionEvaluationException e) {
            logger.error("Failed to evaluate output expression: {}", expression, e);
            
            // Fallback to full data if expression fails
            if (actionDef.shouldContinueOnError()) {
                logger.warn("Returning full action result due to output expression failure");
                return fullData;
            }
            
            throw new ActionException(
                actionDef.getActionId(),
                "Failed to evaluate output expression: " + e.getMessage(),
                e
            );
            
        } finally {
            // Clean up the result variable
            context.removeVariable("result");
        }
    }
    
    /**
     * Evaluate transitions to determine the next rule.
     */
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
                
                // Record transition evaluation
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
                    
                    // Apply context transformation if specified
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
    
    /**
     * Apply context transformation during transition.
     */
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
     * Handle action error by routing to error handler if configured.
     */
    private String handleActionError(RuleDefinition rule, ActionException error, 
                                     ExecutionContext context) {
        
        // Set error info in context
        ErrorInfo errorInfo = ErrorInfo.builder()
                .ruleId(rule.getRuleId())
                .actionId(error.getActionId())
                .message(error.getMessage())
                .errorType("ACTION_ERROR")
                .exception(error)
                .build();
        
        context.setError(errorInfo);
        
        // Record error
        context.addExecutionStep(
            ExecutionStep.builder(ExecutionStep.StepType.ERROR_OCCURRED)
                .ruleId(rule.getRuleId())
                .actionId(error.getActionId())
                .metadata("error", error.getMessage())
                .build()
        );
        
        // Check for action-level error handler
        // (Would need to find the specific action definition - skipping for now)
        
        // Fall back to default error rule if configured
        if (defaultErrorRule != null) {
            logger.info("Routing to default error rule: {}", defaultErrorRule);
            return defaultErrorRule;
        }
        
        // No error handler configured
        return null;
    }
    
    /**
     * Get the rule engine configuration.
     */
    public RuleEngineConfig getConfig() {
        return config;
    }
}