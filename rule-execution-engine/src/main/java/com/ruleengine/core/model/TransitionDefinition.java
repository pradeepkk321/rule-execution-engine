package com.ruleengine.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a transition definition from one rule to another.
 * Transitions are evaluated in priority order after rule actions complete.
 */
public class TransitionDefinition {
    
    @JsonProperty("condition")
    private String condition;
    
    @JsonProperty("targetRule")
    private String targetRule;
    
    @JsonProperty("priority")
    private Integer priority = 0;
    
    @JsonProperty("contextTransform")
    private Map<String, String> contextTransform;
    
    @JsonProperty("terminal")
    private Boolean terminal = false;
    
    // Constructors
    
    public TransitionDefinition() {
    }
    
    // Getters and Setters
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        this.condition = condition;
    }
    
    public String getTargetRule() {
        return targetRule;
    }
    
    public void setTargetRule(String targetRule) {
        this.targetRule = targetRule;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority != null ? priority : 0;
    }
    
    public Map<String, String> getContextTransform() {
        return contextTransform;
    }
    
    public void setContextTransform(Map<String, String> contextTransform) {
        this.contextTransform = contextTransform;
    }
    
    public boolean hasContextTransform() {
        return contextTransform != null && !contextTransform.isEmpty();
    }
    
    public Boolean getTerminal() {
        return terminal;
    }
    
    public void setTerminal(Boolean terminal) {
        this.terminal = terminal != null ? terminal : false;
    }
    
    public boolean isTerminal() {
        return Boolean.TRUE.equals(terminal);
    }
    
    // Validation
    
    /**
     * Check if this transition is valid.
     */
    public boolean isValid() {
        return condition != null && !condition.trim().isEmpty() &&
               targetRule != null && !targetRule.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        return "TransitionDefinition{" +
                "condition='" + condition + '\'' +
                ", targetRule='" + targetRule + '\'' +
                ", priority=" + priority +
                ", terminal=" + terminal +
                '}';
    }
}

