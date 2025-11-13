package com.ruleengine.core.context;

import java.util.*;

/**
 * Mutable execution context that holds the state during rule execution.
 * Contains variables, resources, execution history, and error information.
 * 
 * NOTE: This context is NOT thread-safe. Each rule execution should have its own context instance.
 */
public class ExecutionContext {
    
    // Variables set during execution (results, intermediate values)
    private final Map<String, Object> variables = new HashMap<>();
    
    // Resources provided by the user (HTTP clients, DB connections, caches, etc.)
    private final Map<String, Object> resources = new HashMap<>();
    
    // Execution history for debugging and audit
    private final List<ExecutionStep> executionHistory = new ArrayList<>();
    
    // Error information if execution fails
    private ErrorInfo error;
    
    // Current execution depth (to prevent infinite loops)
    private int depth = 0;
    
    // Current rule being executed
    private String currentRuleId;
    
    // ====================
    // Variable Management
    // ====================
    
    /**
     * Set a variable in the context.
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }
    
    /**
     * Get a variable from the context.
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }
    
    /**
     * Get a variable with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String key, Class<T> type) {
        Object value = variables.get(key);
        if (value == null) {
            return null;
        }
        if (!type.isInstance(value)) {
            throw new ClassCastException("Variable '" + key + "' is not of type " + type.getName());
        }
        return (T) value;
    }
    
    /**
     * Get a variable as Optional.
     */
    public <T> Optional<T> getVariableOptional(String key, Class<T> type) {
        return Optional.ofNullable(getVariable(key, type));
    }
    
    /**
     * Check if a variable exists.
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }
    
    /**
     * Remove a variable from the context.
     */
    public void removeVariable(String key) {
        variables.remove(key);
    }
    
    /**
     * Get all variables as unmodifiable map.
     */
    public Map<String, Object> getAllVariables() {
        return Collections.unmodifiableMap(variables);
    }
    
    /**
     * Set multiple variables at once.
     */
    public void setVariables(Map<String, Object> vars) {
        variables.putAll(vars);
    }
    
    // ====================
    // Resource Management
    // ====================
    
    /**
     * Register a resource (HTTP client, DB connection, cache, etc.).
     */
    public void registerResource(String name, Object resource) {
        resources.put(name, resource);
    }
    
    /**
     * Get a resource with type casting.
     */
    @SuppressWarnings("unchecked")
    public <T> T getResource(String name, Class<T> type) {
        Object resource = resources.get(name);
        if (resource == null) {
            throw new ResourceNotFoundException("Resource not found: " + name);
        }
        if (!type.isInstance(resource)) {
            throw new ClassCastException("Resource '" + name + "' is not of type " + type.getName());
        }
        return (T) resource;
    }
    
    /**
     * Get a resource as Optional.
     */
    public <T> Optional<T> getResourceOptional(String name, Class<T> type) {
        try {
            return Optional.ofNullable(getResource(name, type));
        } catch (ResourceNotFoundException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Check if a resource exists.
     */
    public boolean hasResource(String name) {
        return resources.containsKey(name);
    }
    
    /**
     * Get all resource names.
     */
    public Set<String> getResourceNames() {
        return Collections.unmodifiableSet(resources.keySet());
    }
    
    // ====================
    // Execution Tracking
    // ====================
    
    /**
     * Add an execution step to the history.
     */
    public void addExecutionStep(ExecutionStep step) {
        executionHistory.add(step);
    }
    
    /**
     * Get the execution history.
     */
    public List<ExecutionStep> getExecutionHistory() {
        return Collections.unmodifiableList(executionHistory);
    }
    
    /**
     * Get the current execution depth.
     */
    public int getDepth() {
        return depth;
    }
    
    /**
     * Increment the execution depth (called when entering a new rule).
     */
    public void incrementDepth() {
        depth++;
    }
    
    /**
     * Get the current rule being executed.
     */
    public String getCurrentRuleId() {
        return currentRuleId;
    }
    
    /**
     * Set the current rule being executed.
     */
    public void setCurrentRuleId(String ruleId) {
        this.currentRuleId = ruleId;
    }
    
    // ====================
    // Error Handling
    // ====================
    
    /**
     * Set error information.
     */
    public void setError(ErrorInfo error) {
        this.error = error;
    }
    
    /**
     * Get error information.
     */
    public ErrorInfo getError() {
        return error;
    }
    
    /**
     * Check if context has an error.
     */
    public boolean hasError() {
        return error != null;
    }
    
    /**
     * Clear the error.
     */
    public void clearError() {
        this.error = null;
    }
    
    // ====================
    // Utility Methods
    // ====================
    
    /**
     * Create a snapshot of current variables (for debugging).
     */
    public Map<String, Object> snapshotVariables() {
        return new HashMap<>(variables);
    }
    
    @Override
    public String toString() {
        return "ExecutionContext{" +
                "variables=" + variables.keySet() +
                ", resources=" + resources.keySet() +
                ", depth=" + depth +
                ", currentRule='" + currentRuleId + '\'' +
                ", hasError=" + hasError() +
                '}';
    }
}