package com.ruleengine.core.validation;

import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;
import com.ruleengine.core.model.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Detects cycles in the rule graph that could lead to infinite loops.
 * Uses DFS with coloring algorithm to detect cycles.
 */
public class CycleDetector implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CycleDetector.class);
    
    private enum NodeColor {
        WHITE,  // Not visited
        GRAY,   // Currently being processed (in the recursion stack)
        BLACK   // Finished processing
    }
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null) {
            result.addError("CYCLE-001", "Configuration is null");
            return result;
        }
        
        if (config.getRuleCount() == 0) {
            result.addWarning("CYCLE-002", "No rules to validate");
            return result;
        }
        
        logger.debug("Starting cycle detection on {} rules", config.getRuleCount());
        
        // Build the rule graph
        Map<String, Set<String>> graph = buildRuleGraph(config);
        
        // Detect cycles using DFS
        Map<String, NodeColor> colors = new HashMap<>();
        List<List<String>> cycles = new ArrayList<>();
        Map<String, String> parent = new HashMap<>();
        
        // Initialize all nodes as WHITE
        for (String ruleId : config.getAllRuleIds()) {
            colors.put(ruleId, NodeColor.WHITE);
        }
        
        // Run DFS from each unvisited node
        for (String ruleId : config.getAllRuleIds()) {
            if (colors.get(ruleId) == NodeColor.WHITE) {
                detectCyclesFromNode(ruleId, graph, colors, parent, cycles, new LinkedList<>());
            }
        }
        
        // Report results
        if (cycles.isEmpty()) {
            result.addInfo("CYCLE-003", "No cycles detected in rule graph");
            logger.debug("No cycles found");
        } else {
            result.addWarning("CYCLE-004", 
                "Found " + cycles.size() + " cycle(s) in rule graph. " +
                "This may lead to infinite loops if conditions are not properly designed.",
                "cycles=" + formatCycles(cycles));
            
            logger.info("Detected {} cycles: {}", cycles.size(), formatCycles(cycles));
            
            // Add individual warnings for each cycle
            for (int i = 0; i < cycles.size(); i++) {
                List<String> cycle = cycles.get(i);
                result.addWarning("CYCLE-005", 
                    "Cycle " + (i + 1) + ": " + formatCycle(cycle),
                    "cycleRules=" + cycle);
            }
        }
        
        return result;
    }
    
    /**
     * Build a directed graph of rule transitions.
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
        
        return graph;
    }
    
    /**
     * Detect cycles using DFS with coloring.
     * Returns true if a cycle is detected.
     */
    private boolean detectCyclesFromNode(
            String node,
            Map<String, Set<String>> graph,
            Map<String, NodeColor> colors,
            Map<String, String> parent,
            List<List<String>> cycles,
            LinkedList<String> path) {
        
        colors.put(node, NodeColor.GRAY);
        path.addLast(node);
        
        Set<String> neighbors = graph.getOrDefault(node, Collections.emptySet());
        
        for (String neighbor : neighbors) {
            NodeColor neighborColor = colors.get(neighbor);
            
            if (neighborColor == null) {
                // Skip if neighbor doesn't exist (validation should catch this)
                continue;
            }
            
            if (neighborColor == NodeColor.GRAY) {
                // Back edge found - cycle detected!
                List<String> cycle = extractCycle(path, neighbor);
                cycles.add(cycle);
                logger.debug("Cycle detected: {}", cycle);
                
                // Continue checking for more cycles
                continue;
            }
            
            if (neighborColor == NodeColor.WHITE) {
                parent.put(neighbor, node);
                detectCyclesFromNode(neighbor, graph, colors, parent, cycles, path);
            }
        }
        
        colors.put(node, NodeColor.BLACK);
        path.removeLast();
        
        return !cycles.isEmpty();
    }
    
    /**
     * Extract the cycle from the path.
     */
    private List<String> extractCycle(LinkedList<String> path, String cycleStart) {
        List<String> cycle = new ArrayList<>();
        boolean inCycle = false;
        
        for (String node : path) {
            if (node.equals(cycleStart)) {
                inCycle = true;
            }
            if (inCycle) {
                cycle.add(node);
            }
        }
        
        // Add the cycle start again to show the loop
        cycle.add(cycleStart);
        
        return cycle;
    }
    
    /**
     * Format a single cycle for display.
     */
    private String formatCycle(List<String> cycle) {
        return String.join(" -> ", cycle);
    }
    
    /**
     * Format all cycles for display.
     */
    private String formatCycles(List<List<String>> cycles) {
        return cycles.stream()
                .map(this::formatCycle)
                .collect(Collectors.joining("; "));
    }
}

