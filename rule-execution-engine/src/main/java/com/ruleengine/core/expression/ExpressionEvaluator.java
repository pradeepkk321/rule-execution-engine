package com.ruleengine.core.expression;

import com.ruleengine.core.context.ExecutionContext;

/**
 * Interface for expression evaluation.
 * Allows different expression language implementations (JEXL, SpEL, etc.)
 */
public interface ExpressionEvaluator {
    
    /**
     * Evaluate an expression against the execution context.
     * 
     * @param expression The expression to evaluate
     * @param context The execution context with variables
     * @return The result of the expression evaluation
     * @throws ExpressionEvaluationException if evaluation fails
     */
    Object evaluate(String expression, ExecutionContext context) throws ExpressionEvaluationException;
    
    /**
     * Evaluate an expression and cast the result to the specified type.
     * 
     * @param expression The expression to evaluate
     * @param context The execution context
     * @param resultType The expected result type
     * @return The result cast to the specified type
     * @throws ExpressionEvaluationException if evaluation fails or type casting fails
     */
    <T> T evaluate(String expression, ExecutionContext context, Class<T> resultType) 
            throws ExpressionEvaluationException;
    
    /**
     * Evaluate a boolean expression (for conditions).
     * 
     * @param expression The boolean expression
     * @param context The execution context
     * @return true if the condition evaluates to true
     * @throws ExpressionEvaluationException if evaluation fails
     */
    boolean evaluateBoolean(String expression, ExecutionContext context) 
            throws ExpressionEvaluationException;
    
    /**
     * Compile an expression for reuse.
     * Compiled expressions can be cached for better performance.
     * 
     * @param expression The expression to compile
     * @return A compiled expression object
     * @throws ExpressionEvaluationException if compilation fails
     */
    CompiledExpression compile(String expression) throws ExpressionEvaluationException;
    
    /**
     * Check if an expression is valid without evaluating it.
     * 
     * @param expression The expression to validate
     * @return true if the expression syntax is valid
     */
    boolean isValid(String expression);
}