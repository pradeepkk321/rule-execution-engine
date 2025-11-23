package com.ruleengine.core.action.builtin;

import com.ruleengine.core.action.Action;
import com.ruleengine.core.action.ActionException;
import com.ruleengine.core.action.ActionResult;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.CompiledExpression;
import com.ruleengine.core.expression.ExpressionEvaluationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action that executes a JEXL script/expression.
 * This is the only built-in action in the core library.
 * 
 * Configuration:
 * {
 *   "type": "SCRIPT",
 *   "config": {
 *     "expression": "amount * 1.1 + fee"
 *   }
 * }
 */
public class ScriptAction implements Action {
    
    private static final Logger logger = LoggerFactory.getLogger(ScriptAction.class);
    
    private final String actionId;
    private final CompiledExpression compiledExpression;
    private final String expressionString;
    
    /**
     * Create a script action with a compiled expression.
     * 
     * @param actionId The action identifier
     * @param compiledExpression The compiled JEXL expression
     * @param expressionString The original expression string (for logging)
     */
    public ScriptAction(String actionId, CompiledExpression compiledExpression, String expressionString) {
        this.actionId = actionId;
        this.compiledExpression = compiledExpression;
        this.expressionString = expressionString;
    }
    
    @Override
    public ActionResult execute(ExecutionContext context) throws ActionException {
        logger.debug("Executing script action '{}': {}", actionId, expressionString);
        
        try {
            // Evaluate the expression against the context
            Object result = compiledExpression.evaluate(context);
            
            logger.debug("Script action '{}' completed successfully. Result: {}", actionId, result);
            
            return ActionResult.success(result);
            
        } catch (ExpressionEvaluationException e) {
            logger.error("Script action '{}' failed to evaluate expression: {}", 
                        actionId, expressionString, e);
            
            throw new ActionException(
                actionId,
                "Failed to evaluate script expression: " + e.getMessage(),
                e
            );
        } catch (Exception e) {
            logger.error("Script action '{}' encountered unexpected error", actionId, e);
            
            throw new ActionException(
                actionId,
                "Unexpected error during script execution: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public String getType() {
        return "SCRIPT";
    }
    
    @Override
    public String getActionId() {
        return actionId;
    }
    
    /**
     * Get the expression string (for debugging).
     */
    public String getExpressionString() {
        return expressionString;
    }
    
    @Override
    public String toString() {
        return "ScriptAction{" +
                "actionId='" + actionId + '\'' +
                ", expression='" + expressionString + '\'' +
                '}';
    }
}

