package com.ruleengine.core.expression;

import com.ruleengine.core.context.ExecutionContext;

/**
 * Represents a compiled expression that can be evaluated multiple times.
 * Compiled expressions are cached for better performance.
 */
public interface CompiledExpression {
    
    /**
     * Evaluate the compiled expression against the context.
     * 
     * @param context The execution context
     * @return The evaluation result
     * @throws ExpressionEvaluationException if evaluation fails
     */
    Object evaluate(ExecutionContext context) throws ExpressionEvaluationException;
    
    /**
     * Evaluate the compiled expression and cast to a specific type.
     * 
     * @param context The execution context
     * @param resultType The expected result type
     * @return The result cast to the specified type
     * @throws ExpressionEvaluationException if evaluation or casting fails
     */
    <T> T evaluate(ExecutionContext context, Class<T> resultType) 
            throws ExpressionEvaluationException;
    
    /**
     * Get the original expression string.
     */
    String getExpressionString();
}