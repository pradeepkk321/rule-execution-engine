package com.ruleengine.core.action;

import com.ruleengine.core.model.ActionDefinition;

/**
 * Service Provider Interface (SPI) for creating custom actions.
 * Users implement this interface to provide their own action types.
 * 
 * Multiple providers can support the same action type - they are prioritized
 * by the getPriority() method (higher priority = higher precedence).
 */
public interface ActionProvider {
    
    /**
     * Check if this provider supports the given action type.
     * 
     * @param actionType The action type from the rule definition (e.g., "API", "DATABASE")
     * @return true if this provider can create actions of this type
     */
    boolean supports(String actionType);
    
    /**
     * Create an action instance from the action definition.
     * This method is only called if supports() returns true.
     * 
     * @param definition The action definition from the rule configuration
     * @return A new Action instance
     * @throws ActionCreationException if the action cannot be created
     */
    Action createAction(ActionDefinition definition) throws ActionCreationException;
    
    /**
     * Get the priority of this provider.
     * When multiple providers support the same action type, the one with
     * the highest priority is used.
     * 
     * Default priorities:
     * - Built-in actions: 0
     * - Framework integrations (Spring, etc.): 100
     * - User-defined actions: 200+
     * 
     * @return The priority (higher = more precedence)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Optional: Get a human-readable name for this provider.
     * Used for logging and debugging.
     */
    default String getProviderName() {
        return this.getClass().getSimpleName();
    }
}