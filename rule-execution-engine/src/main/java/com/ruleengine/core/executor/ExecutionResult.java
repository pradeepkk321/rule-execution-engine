package com.ruleengine.core.executor;

import com.ruleengine.core.context.ExecutionContext;

/**
 * Result of a rule execution.
 * Contains the final execution context and status information.
 */
public class ExecutionResult {
    
    private final boolean success;
    private final ExecutionContext context;
    private final String finalRuleId;
    private final String errorMessage;
    private final long executionTimeMs;
    
    private ExecutionResult(Builder builder) {
        this.success = builder.success;
        this.context = builder.context;
        this.finalRuleId = builder.finalRuleId;
        this.errorMessage = builder.errorMessage;
        this.executionTimeMs = builder.executionTimeMs;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public ExecutionContext getContext() {
        return context;
    }
    
    public String getFinalRuleId() {
        return finalRuleId;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public long getExecutionTimeMs() {
        return executionTimeMs;
    }
    
    @Override
    public String toString() {
        return "ExecutionResult{" +
                "success=" + success +
                ", finalRuleId='" + finalRuleId + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                (errorMessage != null ? ", error='" + errorMessage + '\'' : "") +
                '}';
    }
    
    /**
     * Builder for ExecutionResult.
     */
    public static class Builder {
        private boolean success;
        private ExecutionContext context;
        private String finalRuleId;
        private String errorMessage;
        private long executionTimeMs;
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder context(ExecutionContext context) {
            this.context = context;
            return this;
        }
        
        public Builder finalRuleId(String finalRuleId) {
            this.finalRuleId = finalRuleId;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }
        
        public ExecutionResult build() {
            return new ExecutionResult(this);
        }
    }
}

