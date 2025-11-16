package com.ruleengine.core.validation;

import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;
import com.ruleengine.core.model.TransitionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Validates that all rule references are valid.
 * Checks:
 * - Entry point exists
 * - All transition target rules exist
 * - All error handler target rules exist
 * - All referenced rules are defined
 */
public class ReferenceValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReferenceValidator.class);
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null) {
            result.addError("REF-001", "Configuration is null");
            return result;
        }
        
        logger.debug("Starting reference validation");
        
        // Collect all defined rule IDs
        Set<String> definedRules = new HashSet<>(config.getAllRuleIds());
        
        if (definedRules.isEmpty()) {
            result.addError("REF-002", "No rules defined in configuration");
            return result;
        }
        
        logger.debug("Found {} defined rules: {}", definedRules.size(), definedRules);
        
        // Validate entry point
        validateEntryPoint(config, definedRules, result);
        
        // Validate all rule references
        for (RuleDefinition rule : config.getRules()) {
            validateRule(rule, definedRules, result);
        }
        
        // Validate default error rule if specified
        validateDefaultErrorRule(config, definedRules, result);
        
        logger.debug("Reference validation complete: errors={}, warnings={}", 
                    result.getErrorCount(), result.getWarningCount());
        
        return result;
    }
    
    /**
     * Validate that entry point exists.
     */
    private void validateEntryPoint(RuleEngineConfig config, Set<String> definedRules, 
                                    ValidationResult result) {
        
        String entryPoint = config.getEntryPoint();
        
        if (entryPoint == null || entryPoint.trim().isEmpty()) {
            result.addError("REF-003", "Entry point is not specified");
            return;
        }
        
        if (!definedRules.contains(entryPoint)) {
            result.addError("REF-004", 
                "Entry point rule does not exist: " + entryPoint,
                "entryPoint=" + entryPoint);
        }
    }
    
    /**
     * Validate all references within a rule.
     */
    private void validateRule(RuleDefinition rule, Set<String> definedRules, 
                             ValidationResult result) {
        
        String ruleId = rule.getRuleId();
        
        // Validate transition references
        if (rule.hasTransitions()) {
            for (TransitionDefinition transition : rule.getTransitions()) {
                validateTransition(ruleId, transition, definedRules, result);
            }
        }
        
        // Validate action error handler references
        if (rule.hasActions()) {
            for (ActionDefinition action : rule.getActions()) {
                validateActionErrorHandler(ruleId, action, definedRules, result);
            }
        }
        
        // Check if non-terminal rule has no transitions
        if (!rule.isTerminal() && !rule.hasTransitions()) {
            result.addWarning("REF-005", 
                "Non-terminal rule has no transitions, may cause execution to stop",
                "ruleId=" + ruleId);
        }
    }
    
    /**
     * Validate a transition's target rule.
     */
    private void validateTransition(String sourceRuleId, TransitionDefinition transition, 
                                    Set<String> definedRules, ValidationResult result) {
        
        String targetRule = transition.getTargetRule();
        
        if (targetRule == null || targetRule.trim().isEmpty()) {
            result.addError("REF-006", 
                "Transition has empty target rule",
                "sourceRule=" + sourceRuleId + ", condition=" + transition.getCondition());
            return;
        }
        
        if (!definedRules.contains(targetRule)) {
            result.addError("REF-007", 
                "Transition references non-existent rule: " + targetRule,
                "sourceRule=" + sourceRuleId + ", targetRule=" + targetRule);
        }
    }
    
    /**
     * Validate action error handler reference.
     */
    private void validateActionErrorHandler(String ruleId, ActionDefinition action, 
                                           Set<String> definedRules, ValidationResult result) {
        
        if (action.hasErrorHandler()) {
            String targetRule = action.getOnError().getTargetRule();
            
            if (targetRule == null || targetRule.trim().isEmpty()) {
                result.addError("REF-008", 
                    "Action error handler has empty target rule",
                    "ruleId=" + ruleId + ", actionId=" + action.getActionId());
                return;
            }
            
            if (!definedRules.contains(targetRule)) {
                result.addError("REF-009", 
                    "Action error handler references non-existent rule: " + targetRule,
                    "ruleId=" + ruleId + ", actionId=" + action.getActionId() + 
                    ", errorHandlerTarget=" + targetRule);
            }
        }
    }
    
    /**
     * Validate default error rule if specified.
     */
    private void validateDefaultErrorRule(RuleEngineConfig config, Set<String> definedRules, 
                                         ValidationResult result) {
        
        if (config.getGlobalSettings().hasDefaultErrorRule()) {
            String defaultErrorRule = config.getGlobalSettings().getDefaultErrorRule();
            
            if (!definedRules.contains(defaultErrorRule)) {
                result.addError("REF-010", 
                    "Default error rule does not exist: " + defaultErrorRule,
                    "defaultErrorRule=" + defaultErrorRule);
            }
        }
    }
}