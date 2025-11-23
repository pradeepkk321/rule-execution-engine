package com.ruleengine.core.executor;

/**
 * Exception thrown when rule execution fails.
 */
public class RuleExecutionException extends Exception {
    
    private final String ruleId;
    
    public RuleExecutionException(String message) {
        super(message);
        this.ruleId = null;
    }
    
    public RuleExecutionException(String message, Throwable cause) {
        super(message, cause);
        this.ruleId = null;
    }
    
    public RuleExecutionException(String ruleId, String message) {
        super(message);
        this.ruleId = ruleId;
    }
    
    public RuleExecutionException(String ruleId, String message, Throwable cause) {
        super(message, cause);
        this.ruleId = ruleId;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    @Override
    public String toString() {
        if (ruleId != null) {
            return "RuleExecutionException{ruleId='" + ruleId + "', message='" + getMessage() + "'}";
        }
        return super.toString();
    }
}

