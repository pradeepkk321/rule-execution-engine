package com.ruleengine.core.validation;

import com.ruleengine.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Validates duplicate action IDs within rules.
 */
public class DuplicateActionValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(DuplicateActionValidator.class);
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null || config.getRules() == null) {
            return result;
        }
        
        for (RuleDefinition rule : config.getRules()) {
            validateRuleActions(rule, result);
        }
        
        return result;
    }
    
    private void validateRuleActions(RuleDefinition rule, ValidationResult result) {
        if (rule.getActions() == null || rule.getActions().isEmpty()) {
            return;
        }
        
        Map<String, Long> actionIdCounts = rule.getActions().stream()
                .map(ActionDefinition::getActionId)
                .collect(Collectors.groupingBy(id -> id, Collectors.counting()));
        
        actionIdCounts.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .forEach(entry -> {
                    result.addError("DUP-001",
                        "Duplicate action ID '" + entry.getKey() + "' found " + 
                        entry.getValue() + " times in rule: " + rule.getRuleId(),
                        "ruleId=" + rule.getRuleId() + ", actionId=" + entry.getKey());
                    
                    logger.warn("Duplicate action ID detected: {} in rule {}", 
                               entry.getKey(), rule.getRuleId());
                });
    }
}
