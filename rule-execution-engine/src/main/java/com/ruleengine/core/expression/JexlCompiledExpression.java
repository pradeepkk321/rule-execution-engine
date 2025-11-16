package com.ruleengine.core.expression;

import com.ruleengine.core.context.ExecutionContext;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.MapContext;

/**
 * JEXL implementation of compiled expression.
 */
class JexlCompiledExpression implements CompiledExpression {
    
    private final String expressionString;
    private final JexlExpression jexlExpression;
    private final JexlExpressionEvaluator evaluator;
    
    JexlCompiledExpression(String expressionString, JexlExpression jexlExpression, 
                          JexlExpressionEvaluator evaluator) {
        this.expressionString = expressionString;
        this.jexlExpression = jexlExpression;
        this.evaluator = evaluator;
    }
    
    @Override
    public Object evaluate(ExecutionContext context) throws ExpressionEvaluationException {
        try {
            JexlContext jexlContext = createJexlContext(context);
            return jexlExpression.evaluate(jexlContext);
            
        } catch (JexlException e) {
            throw new ExpressionEvaluationException(
                expressionString,
                "Failed to evaluate compiled expression: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public <T> T evaluate(ExecutionContext context, Class<T> resultType) 
            throws ExpressionEvaluationException {
        
        Object result = evaluate(context);
        
        if (result == null) {
            return null;
        }
        
        try {
            return resultType.cast(result);
        } catch (ClassCastException e) {
            throw new ExpressionEvaluationException(
                expressionString,
                "Result type mismatch: expected " + resultType.getName() + 
                " but got " + result.getClass().getName(),
                e
            );
        }
    }
    
    @Override
    public String getExpressionString() {
        return expressionString;
    }
    
    /**
     * Create a JEXL context from execution context.
     */
    private JexlContext createJexlContext(ExecutionContext context) {
        MapContext jexlContext = new MapContext();
        
        // Add all variables from execution context
        context.getAllVariables().forEach(jexlContext::set);
        
        // Add special context variable
        jexlContext.set("context", context);
        
        // CRITICAL: Add util as a regular variable for dot notation access
        jexlContext.set("util", new JexlUtilityFunctions());
        
        return jexlContext;
    }
    
    @Override
    public String toString() {
        return "JexlCompiledExpression{expression='" + expressionString + "'}";
    }
}