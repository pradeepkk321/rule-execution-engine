package com.ruleengine.core.action.builtin;

import com.ruleengine.core.action.Action;
import com.ruleengine.core.action.ActionCreationException;
import com.ruleengine.core.action.ActionProvider;
import com.ruleengine.core.expression.CompiledExpression;
import com.ruleengine.core.expression.ExpressionEvaluator;
import com.ruleengine.core.expression.ExpressionEvaluationException;
import com.ruleengine.core.model.ActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for script actions.
 * Creates ScriptAction instances from action definitions.
 */
public class ScriptActionProvider implements ActionProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ScriptActionProvider.class);
    private static final String ACTION_TYPE = "SCRIPT";
    private static final String CONFIG_KEY_EXPRESSION = "expression";
    
    private final ExpressionEvaluator expressionEvaluator;
    
    /**
     * Create a script action provider with the given expression evaluator.
     * 
     * @param expressionEvaluator The expression evaluator to use for compiling scripts
     */
    public ScriptActionProvider(ExpressionEvaluator expressionEvaluator) {
        if (expressionEvaluator == null) {
            throw new IllegalArgumentException("Expression evaluator cannot be null");
        }
        this.expressionEvaluator = expressionEvaluator;
    }
    
    @Override
    public boolean supports(String actionType) {
        return ACTION_TYPE.equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) throws ActionCreationException {
        String actionId = definition.getActionId();
        
        logger.debug("Creating script action: {}", actionId);
        
        // Validate action definition
        validateDefinition(definition);
        
        // Extract expression from config
        String expression = extractExpression(definition);
        
        // Compile the expression
        CompiledExpression compiledExpression = compileExpression(expression, actionId);
        
        // Create and return the action
        ScriptAction action = new ScriptAction(actionId, compiledExpression, expression);
        
        logger.debug("Successfully created script action: {}", actionId);
        
        return action;
    }
    
    @Override
    public int getPriority() {
        return 0; // Built-in action, default priority
    }
    
    @Override
    public String getProviderName() {
        return "ScriptActionProvider";
    }
    
    /**
     * Validate the action definition.
     */
    private void validateDefinition(ActionDefinition definition) throws ActionCreationException {
        if (definition == null) {
            throw new ActionCreationException("Action definition cannot be null");
        }
        
        if (definition.getActionId() == null || definition.getActionId().trim().isEmpty()) {
            throw new ActionCreationException(
                ACTION_TYPE,
                null,
                "Action ID is required"
            );
        }
        
        if (definition.getConfig() == null || definition.getConfig().isEmpty()) {
            throw new ActionCreationException(
                ACTION_TYPE,
                definition.getActionId(),
                "Config is required for SCRIPT action"
            );
        }
        
        if (!definition.getConfig().containsKey(CONFIG_KEY_EXPRESSION)) {
            throw new ActionCreationException(
                ACTION_TYPE,
                definition.getActionId(),
                "Config must contain 'expression' key"
            );
        }
    }
    
    /**
     * Extract expression from action definition config.
     */
    private String extractExpression(ActionDefinition definition) throws ActionCreationException {
        Object expressionObj = definition.getConfig().get(CONFIG_KEY_EXPRESSION);
        
        if (expressionObj == null) {
            throw new ActionCreationException(
                ACTION_TYPE,
                definition.getActionId(),
                "Expression cannot be null"
            );
        }
        
        String expression = expressionObj.toString().trim();
        
        if (expression.isEmpty()) {
            throw new ActionCreationException(
                ACTION_TYPE,
                definition.getActionId(),
                "Expression cannot be empty"
            );
        }
        
        return expression;
    }
    
    /**
     * Compile the expression.
     */
    private CompiledExpression compileExpression(String expression, String actionId) 
            throws ActionCreationException {
        
        try {
            logger.debug("Compiling expression for action '{}': {}", actionId, expression);
            
            CompiledExpression compiled = expressionEvaluator.compile(expression);
            
            logger.debug("Successfully compiled expression for action '{}'", actionId);
            
            return compiled;
            
        } catch (ExpressionEvaluationException e) {
            logger.error("Failed to compile expression for action '{}': {}", 
                        actionId, expression, e);
            
            throw new ActionCreationException(
                ACTION_TYPE,
                actionId,
                "Failed to compile expression: " + e.getMessage(),
                e
            );
        }
    }
}

