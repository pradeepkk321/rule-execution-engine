package com.ruleengine.core.expression;

import com.ruleengine.core.context.ExecutionContext;
import org.apache.commons.jexl3.*;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JEXL-based expression evaluator implementation.
 * Supports caching of compiled expressions for performance.
 */
public class JexlExpressionEvaluator implements ExpressionEvaluator {
    
    private static final Logger logger = LoggerFactory.getLogger(JexlExpressionEvaluator.class);
    
    private final JexlEngine jexlEngine;
    private final Map<String, JexlExpression> expressionCache;
    private final boolean cacheEnabled;
    
    /**
     * Create evaluator with default JEXL engine and caching enabled.
     */
    public JexlExpressionEvaluator() {
        this(createDefaultJexlEngine(), true);
    }
    
    /**
     * Create evaluator with custom JEXL engine.
     */
    public JexlExpressionEvaluator(JexlEngine jexlEngine) {
        this(jexlEngine, true);
    }
    
    /**
     * Create evaluator with custom settings.
     */
    public JexlExpressionEvaluator(JexlEngine jexlEngine, boolean cacheEnabled) {
        this.jexlEngine = jexlEngine;
        this.cacheEnabled = cacheEnabled;
        this.expressionCache = cacheEnabled ? new ConcurrentHashMap<>() : null;
    }
    
    /**
     * Create a default JEXL engine with recommended settings.
     */
    private static JexlEngine createDefaultJexlEngine() {
        // Create namespaces map
        Map<String, Object> namespaces = new HashMap<>();
        namespaces.put("util", new JexlUtilityFunctions());
        
        // Use UNRESTRICTED permissions to allow all method calls
        // This is safe because we control what objects are in the context
        JexlPermissions permissions = JexlPermissions.UNRESTRICTED;
        
        return new JexlBuilder()
                .cache(512)
                .strict(false)
                .silent(false)
                .safe(false)
                .permissions(permissions)      // Allow unrestricted access
                .namespaces(namespaces)
                .create();
    }
    
    /**
     * Create custom namespaces for JEXL functions.
     */
    private static Map<String, Object> createNamespaces() {
        Map<String, Object> namespaces = new HashMap<>();
        namespaces.put("util", new JexlUtilityFunctions());
        return namespaces;
    }
    
    @Override
    public Object evaluate(String expression, ExecutionContext context) 
            throws ExpressionEvaluationException {
        
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionEvaluationException("Expression cannot be null or empty");
        }
        
        try {
            // Check if this is a script (contains semicolons, multiple statements, loops)
            boolean isScript = expression.contains(";") || expression.contains("for (");
            
            JexlContext jexlContext = createJexlContext(context);
            Object result;
            
            if (isScript) {
                // Use JexlScript for multi-statement expressions
                JexlScript script = jexlEngine.createScript(expression);
                result = script.execute(jexlContext);
            } else {
                // Use JexlExpression for simple expressions
                JexlExpression jexlExpr = getOrCompileExpression(expression);
                result = jexlExpr.evaluate(jexlContext);
            }
            
            logger.debug("Evaluated expression '{}' = {}", expression, result);
            
            return result;
            
        } catch (JexlException e) {
            throw new ExpressionEvaluationException(
                expression, 
                "Failed to evaluate expression: " + e.getMessage(), 
                e
            );
        }
    }
    
    @Override
    public <T> T evaluate(String expression, ExecutionContext context, Class<T> resultType) 
            throws ExpressionEvaluationException {
        
        Object result = evaluate(expression, context);
        
        if (result == null) {
            return null;
        }
        
        try {
            return resultType.cast(result);
        } catch (ClassCastException e) {
            throw new ExpressionEvaluationException(
                expression,
                "Result type mismatch: expected " + resultType.getName() + 
                " but got " + result.getClass().getName(),
                e
            );
        }
    }
    
    @Override
    public boolean evaluateBoolean(String expression, ExecutionContext context) 
            throws ExpressionEvaluationException {
        
        Object result = evaluate(expression, context);
        
        if (result == null) {
            return false;
        }
        
        if (result instanceof Boolean) {
            return (Boolean) result;
        }
        
        // Try to coerce to boolean
        if (result instanceof Number) {
            return ((Number) result).doubleValue() != 0.0;
        }
        
        if (result instanceof String) {
            String str = (String) result;
            return !str.isEmpty() && !str.equalsIgnoreCase("false");
        }
        
        // Non-null objects are truthy
        return true;
    }
    
    @Override
    public CompiledExpression compile(String expression) throws ExpressionEvaluationException {
        if (expression == null || expression.trim().isEmpty()) {
            throw new ExpressionEvaluationException("Expression cannot be null or empty");
        }
        
        try {
            JexlExpression jexlExpr = jexlEngine.createExpression(expression);
            return new JexlCompiledExpression(expression, jexlExpr, this);
            
        } catch (JexlException e) {
            throw new ExpressionEvaluationException(
                expression,
                "Failed to compile expression: " + e.getMessage(),
                e
            );
        }
    }
    
    @Override
    public boolean isValid(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        try {
            jexlEngine.createExpression(expression);
            return true;
        } catch (JexlException e) {
            logger.debug("Invalid expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }
    
    /**
     * Get or compile and cache an expression.
     */
    private JexlExpression getOrCompileExpression(String expression) throws JexlException {
        if (cacheEnabled) {
            return expressionCache.computeIfAbsent(expression, 
                expr -> jexlEngine.createExpression(expr));
        } else {
            return jexlEngine.createExpression(expression);
        }
    }
    
    /**
     * Create a JEXL context from execution context.
     */
    private JexlContext createJexlContext(ExecutionContext context) {
        MapContext jexlContext = new MapContext();
        
        // Add all variables from execution context
        context.getAllVariables().forEach(jexlContext::set);
        
        // Add special context variable for accessing the context itself
        jexlContext.set("context", context);
        
        // CRITICAL: Add util as a regular variable so it can be accessed with dot notation
        // The namespace registration in the engine allows colon syntax (util:uuid())
        // But adding it here allows dot syntax (util.uuid()) which is more intuitive
        jexlContext.set("util", new JexlUtilityFunctions());
        
        logger.debug("Created JEXL context with {} variables including util", 
                    context.getAllVariables().size() + 2); // +2 for context and util
        
        return jexlContext;
    }
    
    /**
     * Clear the expression cache.
     */
    public void clearCache() {
        if (cacheEnabled && expressionCache != null) {
            expressionCache.clear();
            logger.debug("Expression cache cleared");
        }
    }
    
    /**
     * Get the size of the expression cache.
     */
    public int getCacheSize() {
        return cacheEnabled && expressionCache != null ? expressionCache.size() : 0;
    }
    
    /**
     * Get the underlying JEXL engine.
     */
    public JexlEngine getJexlEngine() {
        return jexlEngine;
    }
}

