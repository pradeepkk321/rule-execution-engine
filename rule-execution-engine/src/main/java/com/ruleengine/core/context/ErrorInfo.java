package com.ruleengine.core.context;

import java.time.Instant;

/**
 * Holds information about an error that occurred during rule execution.
 */
public class ErrorInfo {
    
    private final String ruleId;
    private final String actionId;
    private final String message;
    private final String errorType;
    private final Throwable exception;
    private final Instant timestamp;
    
    private ErrorInfo(Builder builder) {
        this.ruleId = builder.ruleId;
        this.actionId = builder.actionId;
        this.message = builder.message;
        this.errorType = builder.errorType;
        this.exception = builder.exception;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getErrorType() {
        return errorType;
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getStackTrace() {
        if (exception == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(exception.toString()).append("\n");
        for (StackTraceElement element : exception.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ErrorInfo{" +
                "ruleId='" + ruleId + '\'' +
                ", actionId='" + actionId + '\'' +
                ", message='" + message + '\'' +
                ", errorType='" + errorType + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
    
    /**
     * Builder for ErrorInfo.
     */
    public static class Builder {
        private String ruleId;
        private String actionId;
        private String message;
        private String errorType;
        private Throwable exception;
        private Instant timestamp;
        
        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }
        
        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder errorType(String errorType) {
            this.errorType = errorType;
            return this;
        }
        
        public Builder exception(Throwable exception) {
            this.exception = exception;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public ErrorInfo build() {
            return new ErrorInfo(this);
        }
    }
}