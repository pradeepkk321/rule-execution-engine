package com.ruleengine.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the rule engine.
 * Binds to application.yml properties with prefix "rule-engine"
 */
@ConfigurationProperties(prefix = "rule-engine")
public class RuleEngineProperties {
    
    /**
     * Path to the rule configuration file.
     * Can be:
     * - classpath: classpath:rules/config.json
     * - file: file:/path/to/rules.json
     * - http: http://config-server/rules.json
     */
    private String configLocation = "classpath:rules.json";
    
    /**
     * Enable validation of rule configuration on startup.
     */
    private boolean validateOnStartup = true;
    
    /**
     * Maximum execution depth to prevent infinite loops.
     */
    private Integer maxExecutionDepth = 50;
    
    /**
     * Execution timeout in milliseconds.
     */
    private Long executionTimeout = 30000L;
    
    /**
     * Enable execution history tracking.
     */
    private boolean trackExecutionHistory = true;
    
    /**
     * Enable caching of compiled expressions.
     */
    private boolean cacheExpressions = true;
    
    /**
     * Expression cache size.
     */
    private int expressionCacheSize = 512;
    
    // Getters and Setters
    
    public String getConfigLocation() {
        return configLocation;
    }
    
    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }
    
    public boolean isValidateOnStartup() {
        return validateOnStartup;
    }
    
    public void setValidateOnStartup(boolean validateOnStartup) {
        this.validateOnStartup = validateOnStartup;
    }
    
    public Integer getMaxExecutionDepth() {
        return maxExecutionDepth;
    }
    
    public void setMaxExecutionDepth(Integer maxExecutionDepth) {
        this.maxExecutionDepth = maxExecutionDepth;
    }
    
    public Long getExecutionTimeout() {
        return executionTimeout;
    }
    
    public void setExecutionTimeout(Long executionTimeout) {
        this.executionTimeout = executionTimeout;
    }
    
    public boolean isTrackExecutionHistory() {
        return trackExecutionHistory;
    }
    
    public void setTrackExecutionHistory(boolean trackExecutionHistory) {
        this.trackExecutionHistory = trackExecutionHistory;
    }
    
    public boolean isCacheExpressions() {
        return cacheExpressions;
    }
    
    public void setCacheExpressions(boolean cacheExpressions) {
        this.cacheExpressions = cacheExpressions;
    }
    
    public int getExpressionCacheSize() {
        return expressionCacheSize;
    }
    
    public void setExpressionCacheSize(int expressionCacheSize) {
        this.expressionCacheSize = expressionCacheSize;
    }
    
    @Override
    public String toString() {
        return "RuleEngineProperties{" +
                "configLocation='" + configLocation + '\'' +
                ", validateOnStartup=" + validateOnStartup +
                ", maxExecutionDepth=" + maxExecutionDepth +
                ", executionTimeout=" + executionTimeout +
                '}';
    }
}