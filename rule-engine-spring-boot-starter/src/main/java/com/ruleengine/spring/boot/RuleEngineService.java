package com.ruleengine.spring.boot;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.executor.ExecutionResult;
import com.ruleengine.core.executor.RuleExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Spring service wrapper for RuleExecutor.
 * Provides convenient methods for executing rules in Spring applications.
 */
@Service
public class RuleEngineService {
    
    private static final Logger logger = LoggerFactory.getLogger(RuleEngineService.class);
    
    private final RuleExecutor ruleExecutor;
    
    public RuleEngineService(RuleExecutor ruleExecutor) {
        this.ruleExecutor = ruleExecutor;
    }
    
    /**
     * Execute rules with an execution context.
     */
    public ExecutionResult execute(ExecutionContext context) {
        logger.debug("Executing rules with context");
        
        try {
            ExecutionResult result = ruleExecutor.execute(context);
            
            if (result.isSuccess()) {
                logger.debug("Rule execution completed successfully in {}ms", 
                           result.getExecutionTimeMs());
            } else {
                logger.warn("Rule execution failed: {}", result.getErrorMessage());
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Unexpected error during rule execution", e);
            throw new RuleExecutionException("Rule execution failed", e);
        }
    }
    
    /**
     * Execute rules with initial variables.
     */
    public ExecutionResult execute(Map<String, Object> variables) {
        ExecutionContext context = new ExecutionContext();
        
        if (variables != null) {
            context.setVariables(variables);
        }
        
        return execute(context);
    }
    
    /**
     * Execute rules with a single variable.
     */
    public ExecutionResult execute(String key, Object value) {
        ExecutionContext context = new ExecutionContext();
        context.setVariable(key, value);
        
        return execute(context);
    }
    
    /**
     * Execute rules and return a specific result variable.
     */
    public <T> T executeAndGet(ExecutionContext context, String variableName, Class<T> type) {
        ExecutionResult result = execute(context);
        
        if (result.isSuccess()) {
            return result.getContext().getVariable(variableName, type);
        }
        
        throw new RuleExecutionException(
            "Rule execution failed: " + result.getErrorMessage());
    }
    
    /**
     * Execute rules with variables and return a specific result.
     */
    public <T> T executeAndGet(Map<String, Object> variables, 
                               String variableName, 
                               Class<T> type) {
        ExecutionContext context = new ExecutionContext();
        
        if (variables != null) {
            context.setVariables(variables);
        }
        
        return executeAndGet(context, variableName, type);
    }
    
    /**
     * Get the underlying RuleExecutor.
     */
    public RuleExecutor getRuleExecutor() {
        return ruleExecutor;
    }
    
    /**
     * Exception thrown when rule execution fails.
     */
    public static class RuleExecutionException extends RuntimeException {
        
        public RuleExecutionException(String message) {
            super(message);
        }
        
        public RuleExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}