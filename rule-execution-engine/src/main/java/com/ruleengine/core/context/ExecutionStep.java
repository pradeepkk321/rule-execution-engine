package com.ruleengine.core.context;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single step in the rule execution history.
 * Used for debugging, auditing, and tracking execution flow.
 */
public class ExecutionStep {
    
    public enum StepType {
        RULE_ENTERED,
        RULE_EXITED,
        ACTION_STARTED,
        ACTION_COMPLETED,
        ACTION_FAILED,
        TRANSITION_EVALUATED,
        ERROR_OCCURRED
    }
    
    private final StepType type;
    private final String ruleId;
    private final String actionId;
    private final Instant timestamp;
    private final Map<String, Object> metadata;
    private final long durationMs; // Duration for completed actions
    
    private ExecutionStep(Builder builder) {
        this.type = builder.type;
        this.ruleId = builder.ruleId;
        this.actionId = builder.actionId;
        this.timestamp = builder.timestamp != null ? builder.timestamp : Instant.now();
        this.metadata = new HashMap<>(builder.metadata);
        this.durationMs = builder.durationMs;
    }
    
    public static Builder builder(StepType type) {
        return new Builder(type);
    }
    
    public StepType getType() {
        return type;
    }
    
    public String getRuleId() {
        return ruleId;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public long getDurationMs() {
        return durationMs;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ExecutionStep{")
          .append("type=").append(type)
          .append(", timestamp=").append(timestamp);
        
        if (ruleId != null) {
            sb.append(", ruleId='").append(ruleId).append('\'');
        }
        if (actionId != null) {
            sb.append(", actionId='").append(actionId).append('\'');
        }
        if (durationMs > 0) {
            sb.append(", durationMs=").append(durationMs);
        }
        if (!metadata.isEmpty()) {
            sb.append(", metadata=").append(metadata);
        }
        
        sb.append('}');
        return sb.toString();
    }
    
    /**
     * Builder for ExecutionStep.
     */
    public static class Builder {
        private final StepType type;
        private String ruleId;
        private String actionId;
        private Instant timestamp;
        private final Map<String, Object> metadata = new HashMap<>();
        private long durationMs;
        
        private Builder(StepType type) {
            this.type = type;
        }
        
        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }
        
        public Builder actionId(String actionId) {
            this.actionId = actionId;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder metadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            this.metadata.putAll(metadata);
            return this;
        }
        
        public Builder durationMs(long durationMs) {
            this.durationMs = durationMs;
            return this;
        }
        
        public ExecutionStep build() {
            return new ExecutionStep(this);
        }
    }
}

