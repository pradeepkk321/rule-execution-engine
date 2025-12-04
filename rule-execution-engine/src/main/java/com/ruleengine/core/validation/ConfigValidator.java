package com.ruleengine.core.validation;

import com.ruleengine.core.model.RuleEngineConfig;

/**
 * Base interface for all configuration validators.
 */
public interface ConfigValidator {
    
    /**
     * Validate the rule engine configuration.
     * 
     * @param config The configuration to validate
     * @return ValidationResult containing any issues found
     */
    ValidationResult validate(RuleEngineConfig config);
    
    /**
     * Get the name of this validator (for logging).
     */
    default String getValidatorName() {
        return this.getClass().getSimpleName();
    }
}


