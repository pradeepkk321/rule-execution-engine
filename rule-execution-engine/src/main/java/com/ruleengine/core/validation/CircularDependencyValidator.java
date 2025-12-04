package com.ruleengine.core.validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;

/**
 * Validates circular variable dependencies in action outputs.
 */
public class CircularDependencyValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CircularDependencyValidator.class);
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null || config.getRules() == null) {
            return result;
        }
        
        for (RuleDefinition rule : config.getRules()) {
            detectCircularDependencies(rule, result);
        }
        
        return result;
    }
    
    private void detectCircularDependencies(RuleDefinition rule, ValidationResult result) {
        if (rule.getActions() == null) {
            return;
        }
        
        // Build dependency graph
        Map<String, Set<String>> dependencyGraph = new HashMap<>();
        
        for (ActionDefinition action : rule.getActions()) {
            String outputVar = action.getOutputVariable();
            if (outputVar == null) {
                continue;
            }
            
            Set<String> dependencies = extractVariableDependencies(action);
            dependencyGraph.put(outputVar, dependencies);
        }
        
        // Detect cycles
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        
        for (String variable : dependencyGraph.keySet()) {
            if (detectCycle(variable, dependencyGraph, visited, recursionStack, currentPath, result, rule.getRuleId())) {
                // Cycle detected and reported
            }
        }
    }
    
    private Set<String> extractVariableDependencies(ActionDefinition action) {
        Set<String> dependencies = new HashSet<>();
        
        // Check config for variable references (${varName})
        if (action.getConfig() != null) {
            extractFromMap(action.getConfig(), dependencies);
        }
        
        // Check outputExpression
        if (action.hasOutputExpression()) {
            extractVariablesFromExpression(action.getOutputExpression(), dependencies);
        }
        
        return dependencies;
    }
    
    private void extractFromMap(Map<String, Object> map, Set<String> dependencies) {
        for (Object value : map.values()) {
            if (value instanceof String) {
                extractVariablesFromExpression((String) value, dependencies);
            } else if (value instanceof Map) {
                extractFromMap((Map<String, Object>) value, dependencies);
            }
        }
    }
    
    private void extractVariablesFromExpression(String expression, Set<String> dependencies) {
        if (expression == null) {
            return;
        }
        
        // Simple extraction of ${varName} patterns
        int start = 0;
        while ((start = expression.indexOf("${", start)) != -1) {
            int end = expression.indexOf("}", start);
            if (end != -1) {
                String varName = expression.substring(start + 2, end);
                dependencies.add(varName);
                start = end + 1;
            } else {
                break;
            }
        }
        
        // Also check for direct variable names in expressions (simple case)
        // This is a simplified check - full JEXL parsing would be more accurate
    }
    
    private boolean detectCycle(String variable, Map<String, Set<String>> graph,
                                Set<String> visited, Set<String> recursionStack,
                                List<String> currentPath, ValidationResult result, String ruleId) {
        
        if (recursionStack.contains(variable)) {
            // Cycle detected
            int cycleStart = currentPath.indexOf(variable);
            List<String> cycle = new ArrayList<>(currentPath.subList(cycleStart, currentPath.size()));
            cycle.add(variable);
            
            result.addError("CIRC-001",
                "Circular variable dependency detected in rule " + ruleId + ": " + 
                String.join(" -> ", cycle),
                "ruleId=" + ruleId + ", cycle=" + cycle);
            
            logger.warn("Circular dependency detected: {}", cycle);
            return true;
        }
        
        if (visited.contains(variable)) {
            return false;
        }
        
        visited.add(variable);
        recursionStack.add(variable);
        currentPath.add(variable);
        
        Set<String> dependencies = graph.get(variable);
        if (dependencies != null) {
            for (String dependency : dependencies) {
                if (graph.containsKey(dependency)) {
                    if (detectCycle(dependency, graph, visited, recursionStack, currentPath, result, ruleId)) {
                        return true;
                    }
                }
            }
        }
        
        recursionStack.remove(variable);
        currentPath.remove(currentPath.size() - 1);
        
        return false;
    }
}
