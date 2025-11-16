package com.ruleengine.core.action;

import com.ruleengine.core.model.ActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Registry for action providers.
 * Manages multiple action providers and routes action creation to the appropriate provider
 * based on action type and priority.
 */
public class ActionRegistry {
    
    private static final Logger logger = LoggerFactory.getLogger(ActionRegistry.class);
    
    private final List<ActionProvider> providers = new ArrayList<>();
    private boolean sorted = false;
    
    /**
     * Register an action provider.
     * Providers are sorted by priority (highest first) when creating actions.
     * 
     * @param provider The action provider to register
     */
    public void registerProvider(ActionProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("Action provider cannot be null");
        }
        
        providers.add(provider);
        sorted = false;
        
        logger.info("Registered action provider: {} with priority: {}", 
                    provider.getProviderName(), provider.getPriority());
    }
    
    /**
     * Register multiple action providers at once.
     */
    public void registerProviders(List<ActionProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            return;
        }
        
        providers.forEach(this::registerProvider);
    }
    
    /**
     * Create an action from its definition.
     * Finds the first provider (by priority) that supports the action type.
     * 
     * @param definition The action definition
     * @return A new Action instance
     * @throws UnsupportedActionException if no provider supports this action type
     * @throws ActionCreationException if action creation fails
     */
    public Action createAction(ActionDefinition definition) throws ActionCreationException {
        if (definition == null) {
            throw new IllegalArgumentException("Action definition cannot be null");
        }
        
        ensureSorted();
        
        String actionType = definition.getType();
        String actionId = definition.getActionId();
        
        logger.debug("Creating action: type={}, id={}", actionType, actionId);
        
        // Find the first provider that supports this action type
        for (ActionProvider provider : providers) {
            if (provider.supports(actionType)) {
                try {
                    Action action = provider.createAction(definition);
                    logger.debug("Action created by provider: {} for type: {}", 
                                provider.getProviderName(), actionType);
                    return action;
                } catch (ActionCreationException e) {
                    // Re-throw as-is
                    throw e;
                } catch (Exception e) {
                    // Wrap unexpected exceptions
                    throw new ActionCreationException(
                        actionType, 
                        actionId, 
                        "Failed to create action using provider: " + provider.getProviderName(), 
                        e
                    );
                }
            }
        }
        
        // No provider found
        throw new UnsupportedActionException(
            "No action provider found for type: " + actionType + 
            " (actionId: " + actionId + "). " +
            "Available providers: " + getProviderInfo()
        );
    }
    
    /**
     * Check if any provider supports the given action type.
     */
    public boolean supports(String actionType) {
        if (actionType == null) {
            return false;
        }
        
        return providers.stream()
                .anyMatch(provider -> provider.supports(actionType));
    }
    
    /**
     * Get all registered providers.
     */
    public List<ActionProvider> getProviders() {
        return new ArrayList<>(providers);
    }
    
    /**
     * Get providers that support a specific action type.
     */
    public List<ActionProvider> getProvidersFor(String actionType) {
        ensureSorted();
        return providers.stream()
                .filter(provider -> provider.supports(actionType))
                .collect(Collectors.toList());
    }
    
    /**
     * Get information about registered providers (for debugging).
     */
    public String getProviderInfo() {
        ensureSorted();
        return providers.stream()
                .map(p -> p.getProviderName() + " (priority: " + p.getPriority() + ")")
                .collect(Collectors.joining(", "));
    }
    
    /**
     * Get the count of registered providers.
     */
    public int getProviderCount() {
        return providers.size();
    }
    
    /**
     * Clear all registered providers (mainly for testing).
     */
    public void clear() {
        providers.clear();
        sorted = false;
        logger.debug("Cleared all action providers");
    }
    
    /**
     * Ensure providers are sorted by priority (lazy sorting).
     */
    private void ensureSorted() {
        if (!sorted) {
            providers.sort(Comparator.comparingInt(ActionProvider::getPriority).reversed());
            sorted = true;
            
            if (logger.isDebugEnabled()) {
                logger.debug("Sorted action providers by priority: {}", 
                    providers.stream()
                        .map(p -> p.getProviderName() + ":" + p.getPriority())
                        .collect(Collectors.joining(", ")));
            }
        }
    }
}