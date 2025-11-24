package com.ruleengine.core.validation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ruleengine.core.model.ActionDefinition;
import com.ruleengine.core.model.RuleDefinition;
import com.ruleengine.core.model.RuleEngineConfig;

/**
 * Validates conditional action syntax.
 */
public class ConditionalActionValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(ConditionalActionValidator.class);
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (config == null || config.getRules() == null) {
            return result;
        }
        
        for (RuleDefinition rule : config.getRules()) {
            validateConditionalActions(rule, result);
        }
        
        return result;
    }
    
    private void validateConditionalActions(RuleDefinition rule, ValidationResult result) {
        if (rule.getActions() == null) {
            return;
        }
        
        for (ActionDefinition action : rule.getActions()) {
            if (action.hasCondition()) {
                String condition = action.getCondition().trim();
                
                // Check for empty condition
                if (condition.isEmpty()) {
                    result.addWarning("COND-001",
                        "Action has empty condition string",
                        "ruleId=" + rule.getRuleId() + ", actionId=" + action.getActionId());
                }
                
                // Check for potential assignment instead of comparison
                // Look for single = not preceded or followed by another =
                if (hasSingleEquals(condition)) {
                    result.addWarning("COND-002",
                        "Action condition may contain assignment (=) instead of comparison (==). " +
                        "Check: " + condition,
                        "ruleId=" + rule.getRuleId() + ", actionId=" + action.getActionId());
                }
                
                // Check for unbalanced parentheses
                if (hasUnbalancedParentheses(condition)) {
                    result.addError("COND-003",
                        "Action condition has unbalanced parentheses",
                        "ruleId=" + rule.getRuleId() + ", actionId=" + action.getActionId());
                }
                
                // Check for common mistakes
                if (condition.contains("&&") && condition.contains("||") && 
                    !condition.contains("(")) {
                    result.addWarning("COND-004",
                        "Condition mixes && and || without parentheses. Consider adding parentheses for clarity.",
                        "ruleId=" + rule.getRuleId() + ", actionId=" + action.getActionId());
                }
            }
        }
    }
    
    /**
     * Check if string contains single = that's not part of ==, !=, <=, >=
     */
    private boolean hasSingleEquals(String condition) {
        // Remove all valid operators first
        String cleaned = condition
                .replace("==", "")
                .replace("!=", "")
                .replace("<=", "")
                .replace(">=", "");
        
        // If there's still an = left, it's likely a single =
        return cleaned.contains("=");
    }
    
    /**
     * Check for unbalanced parentheses.
     */
    private boolean hasUnbalancedParentheses(String condition) {
        int count = 0;
        for (char c : condition.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) return true; // More closing than opening
        }
        return count != 0; // Should be balanced at end
    }
}
