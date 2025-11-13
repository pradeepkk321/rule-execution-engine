package com.ruleengine.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an action definition from the rule configuration.
 * Maps to the JSON action structure.
 */
public class ActionDefinition {
    
    @JsonProperty("actionId")
    private String actionId;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("config")
    private Map<String, Object> config = new HashMap<>();
    
    @JsonProperty("outputVariable")
    private String outputVariable;
    
    @JsonProperty("outputExpression")
    private String outputExpression;
    
    @JsonProperty("continueOnError")
    private Boolean continueOnError = false;
    
    @JsonProperty("onError")
    private ErrorHandlerDefinition onError;
    
    // Constructors
    
    public ActionDefinition() {
    }
    
    // Getters and Setters
    
    public String getActionId() {
        return actionId;
    }
    
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Map<String, Object> getConfig() {
        return config;
    }
    
    public void setConfig(Map<String, Object> config) {
        this.config = config != null ? config : new HashMap<>();
    }
    
    public String getOutputVariable() {
        return outputVariable;
    }
    
    public void setOutputVariable(String outputVariable) {
        this.outputVariable = outputVariable;
    }
    
    public String getOutputExpression() {
        return outputExpression;
    }
    
    public void setOutputExpression(String outputExpression) {
        this.outputExpression = outputExpression;
    }
    
    public boolean hasOutputExpression() {
        return outputExpression != null && !outputExpression.trim().isEmpty();
    }
    
    public Boolean getContinueOnError() {
        return continueOnError;
    }
    
    public void setContinueOnError(Boolean continueOnError) {
        this.continueOnError = continueOnError != null ? continueOnError : false;
    }
    
    public boolean shouldContinueOnError() {
        return Boolean.TRUE.equals(continueOnError);
    }
    
    public ErrorHandlerDefinition getOnError() {
        return onError;
    }
    
    public void setOnError(ErrorHandlerDefinition onError) {
        this.onError = onError;
    }
    
    public boolean hasErrorHandler() {
        return onError != null && onError.getTargetRule() != null;
    }
    
    // Utility methods
    
    /**
     * Get a config value with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> type) {
        Object value = config.get(key);
        if (value == null) {
            return null;
        }
        return (T) value;
    }
    
    /**
     * Get a config value with default.
     */
    @SuppressWarnings("unchecked")
    public <T> T getConfigValue(String key, Class<T> type, T defaultValue) {
        Object value = config.get(key);
        if (value == null) {
            return defaultValue;
        }
        return (T) value;
    }
    
    @Override
    public String toString() {
        return "ActionDefinition{" +
                "actionId='" + actionId + '\'' +
                ", type='" + type + '\'' +
                ", outputVariable='" + outputVariable + '\'' +
                ", outputExpression='" + outputExpression + '\'' +
                ", continueOnError=" + continueOnError +
                '}';
    }
    
    /**
     * Nested class for error handler configuration.
     */
    public static class ErrorHandlerDefinition {
        
        @JsonProperty("targetRule")
        private String targetRule;
        
        public ErrorHandlerDefinition() {
        }
        
        public String getTargetRule() {
            return targetRule;
        }
        
        public void setTargetRule(String targetRule) {
            this.targetRule = targetRule;
        }
        
        @Override
        public String toString() {
            return "ErrorHandlerDefinition{" +
                    "targetRule='" + targetRule + '\'' +
                    '}';
        }
    }
}