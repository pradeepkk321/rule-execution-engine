package com.ruleengine.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Top-level configuration for the rule engine.
 * Contains global settings and all rule definitions.
 */
public class RuleEngineConfig {
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("entryPoint")
    private String entryPoint;
    
    @JsonProperty("globalSettings")
    private GlobalSettings globalSettings;
    
    @JsonProperty("rules")
    private List<RuleDefinition> rules = new ArrayList<>();
    
    // Constructors
    
    public RuleEngineConfig() {
        this.globalSettings = new GlobalSettings();
    }
    
    // Getters and Setters
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getEntryPoint() {
        return entryPoint;
    }
    
    public void setEntryPoint(String entryPoint) {
        this.entryPoint = entryPoint;
    }
    
    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }
    
    public void setGlobalSettings(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings != null ? globalSettings : new GlobalSettings();
    }
    
    public List<RuleDefinition> getRules() {
        return rules;
    }
    
    public void setRules(List<RuleDefinition> rules) {
        this.rules = rules != null ? rules : new ArrayList<>();
    }
    
    // Utility methods
    
    /**
     * Get a rule by its ID.
     */
    public RuleDefinition getRule(String ruleId) {
        if (ruleId == null || rules == null) {
            return null;
        }
        
        return rules.stream()
                .filter(rule -> ruleId.equals(rule.getRuleId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Check if a rule exists.
     */
    public boolean hasRule(String ruleId) {
        return getRule(ruleId) != null;
    }
    
    /**
     * Get all rule IDs.
     */
    public List<String> getAllRuleIds() {
        return rules.stream()
                .map(RuleDefinition::getRuleId)
                .collect(Collectors.toList());
    }
    
    /**
     * Get all terminal rules.
     */
    public List<RuleDefinition> getTerminalRules() {
        return rules.stream()
                .filter(RuleDefinition::isTerminal)
                .collect(Collectors.toList());
    }
    
    /**
     * Build a map of rules by ID for fast lookup.
     */
    public Map<String, RuleDefinition> buildRuleMap() {
        Map<String, RuleDefinition> ruleMap = new HashMap<>();
        if (rules != null) {
            rules.forEach(rule -> ruleMap.put(rule.getRuleId(), rule));
        }
        return ruleMap;
    }
    
    /**
     * Get the count of rules.
     */
    public int getRuleCount() {
        return rules != null ? rules.size() : 0;
    }
    
    @Override
    public String toString() {
        return "RuleEngineConfig{" +
                "version='" + version + '\'' +
                ", entryPoint='" + entryPoint + '\'' +
                ", ruleCount=" + getRuleCount() +
                ", globalSettings=" + globalSettings +
                '}';
    }
    
    /**
     * Global settings for the rule engine.
     */
    public static class GlobalSettings {
        
        @JsonProperty("maxExecutionDepth")
        private Integer maxExecutionDepth = 50;
        
        @JsonProperty("timeout")
        private Long timeout = 30000L; // 30 seconds
        
        @JsonProperty("defaultErrorRule")
        private String defaultErrorRule;
        
        public GlobalSettings() {
        }
        
        public Integer getMaxExecutionDepth() {
            return maxExecutionDepth;
        }
        
        public void setMaxExecutionDepth(Integer maxExecutionDepth) {
            this.maxExecutionDepth = maxExecutionDepth != null ? maxExecutionDepth : 50;
        }
        
        public Long getTimeout() {
            return timeout;
        }
        
        public void setTimeout(Long timeout) {
            this.timeout = timeout != null ? timeout : 30000L;
        }
        
        public String getDefaultErrorRule() {
            return defaultErrorRule;
        }
        
        public void setDefaultErrorRule(String defaultErrorRule) {
            this.defaultErrorRule = defaultErrorRule;
        }
        
        public boolean hasDefaultErrorRule() {
            return defaultErrorRule != null && !defaultErrorRule.trim().isEmpty();
        }
        
        @Override
        public String toString() {
            return "GlobalSettings{" +
                    "maxExecutionDepth=" + maxExecutionDepth +
                    ", timeout=" + timeout +
                    ", defaultErrorRule='" + defaultErrorRule + '\'' +
                    '}';
        }
    }
}

