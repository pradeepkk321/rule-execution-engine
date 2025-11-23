package com.ruleengine.core.validation;

import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;
import com.ruleengine.core.model.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Validates that all rules are reachable from the entry point.
 * Identifies "dead rules" that can never be executed.
 */
public class ReachabilityValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReachabilityValidator.class);
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null) {
            result.addError("REACH-001", "Configuration is null");
            return result;
        }
        
        String entryPoint = config.getEntryPoint();
        if (entryPoint == null || entryPoint.trim().isEmpty()) {
            result.addWarning("REACH-002", 
                "Cannot check reachability: entry point is not specified");
            return result;
        }
        
        if (!config.hasRule(entryPoint)) {
            result.addError("REACH-003", 
                "Cannot check reachability: entry point rule does not exist: " + entryPoint);
            return result;
        }
        
        logger.debug("Starting reachability analysis from entry point: {}", entryPoint);
        
        // Build the rule graph
        Map<String, Set<String>> graph = buildRuleGraph(config);
        
        // Find all reachable rules using BFS
        Set<String> reachableRules = findReachableRules(entryPoint, graph);
        
        logger.debug("Found {} reachable rules out of {} total rules", 
                    reachableRules.size(), config.getRuleCount());
        
        // Identify unreachable rules
        Set<String> allRules = new HashSet<>(config.getAllRuleIds());
        allRules.removeAll(reachableRules);
        
        if (!allRules.isEmpty()) {
            result.addWarning("REACH-004", 
                "Found " + allRules.size() + " unreachable rule(s): " + allRules,
                "unreachableRules=" + allRules);
            
            logger.info("Unreachable rules detected: {}", allRules);
        } else {
            result.addInfo("REACH-005", 
                "All rules are reachable from entry point");
            logger.debug("All rules are reachable");
        }
        
        return result;
    }
    
    /**
     * Build a directed graph of rule transitions.
     * Returns a map: ruleId -> Set of target rule IDs
     */
    private Map<String, Set<String>> buildRuleGraph(RuleEngineConfig config) {
        Map<String, Set<String>> graph = new HashMap<>();
        
        for (RuleDefinition rule : config.getRules()) {
            String ruleId = rule.getRuleId();
            Set<String> targets = new HashSet<>();
            
            // Add transition targets
            if (rule.hasTransitions()) {
                for (TransitionDefinition transition : rule.getTransitions()) {
                    if (transition.getTargetRule() != null) {
                        targets.add(transition.getTargetRule());
                    }
                }
            }
            
            // Add error handler targets
            if (rule.hasActions()) {
                for (ActionDefinition action : rule.getActions()) {
                    if (action.hasErrorHandler()) {
                        String errorTarget = action.getOnError().getTargetRule();
                        if (errorTarget != null) {
                            targets.add(errorTarget);
                        }
                    }
                }
            }
            
            graph.put(ruleId, targets);
        }
        
        // Add default error rule as reachable from all rules if specified
        if (config.getGlobalSettings().hasDefaultErrorRule()) {
            String defaultErrorRule = config.getGlobalSettings().getDefaultErrorRule();
            // Default error rule is potentially reachable from any rule
            // So we mark it as reachable from entry point for simplicity
            graph.computeIfAbsent(config.getEntryPoint(), k -> new HashSet<>())
                 .add(defaultErrorRule);
        }
        
        return graph;
    }
    
    /**
     * Find all rules reachable from the entry point using BFS.
     */
    private Set<String> findReachableRules(String entryPoint, Map<String, Set<String>> graph) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        
        queue.offer(entryPoint);
        reachable.add(entryPoint);
        
        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<String> neighbors = graph.get(current);
            
            if (neighbors != null) {
                for (String neighbor : neighbors) {
                    if (!reachable.contains(neighbor)) {
                        reachable.add(neighbor);
                        queue.offer(neighbor);
                    }
                }
            }
        }
        
        return reachable;
    }
}

