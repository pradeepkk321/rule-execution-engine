package com.ruleengine.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete rule definition from the configuration.
 * A rule contains actions to execute and transitions to other rules.
 */
public class RuleDefinition {
    
    @JsonProperty("ruleId")
    private String ruleId;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("actions")
    private List<ActionDefinition> actions = new ArrayList<>();
    
    @JsonProperty("transitions")
    private List<TransitionDefinition> transitions = new ArrayList<>();
    
    @JsonProperty("terminal")
    private Boolean terminal = false;
    
    // Constructors
    
    public RuleDefinition() {
    }
    
    // Getters and Setters
    
    public String getRuleId() {
        return ruleId;
    }
    
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public List<ActionDefinition> getActions() {
        return actions;
    }
    
    public void setActions(List<ActionDefinition> actions) {
        this.actions = actions != null ? actions : new ArrayList<>();
    }
    
    public List<TransitionDefinition> getTransitions() {
        return transitions;
    }
    
    public void setTransitions(List<TransitionDefinition> transitions) {
        this.transitions = transitions != null ? transitions : new ArrayList<>();
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
    
    // Utility methods
    
    /**
     * Check if this rule has any actions.
     */
    public boolean hasActions() {
        return actions != null && !actions.isEmpty();
    }
    
    /**
     * Check if this rule has any transitions.
     */
    public boolean hasTransitions() {
        return transitions != null && !transitions.isEmpty();
    }
    
    /**
     * Get the number of actions.
     */
    public int getActionCount() {
        return actions != null ? actions.size() : 0;
    }
    
    /**
     * Get the number of transitions.
     */
    public int getTransitionCount() {
        return transitions != null ? transitions.size() : 0;
    }
    
    /**
     * Get sorted transitions (by priority, descending).
     */
    public List<TransitionDefinition> getSortedTransitions() {
        List<TransitionDefinition> sorted = new ArrayList<>(transitions);
        sorted.sort((t1, t2) -> Integer.compare(t2.getPriority(), t1.getPriority()));
        return sorted;
    }
    
    @Override
    public String toString() {
        return "RuleDefinition{" +
                "ruleId='" + ruleId + '\'' +
                ", description='" + description + '\'' +
                ", actionCount=" + getActionCount() +
                ", transitionCount=" + getTransitionCount() +
                ", terminal=" + terminal +
                '}';
    }
}