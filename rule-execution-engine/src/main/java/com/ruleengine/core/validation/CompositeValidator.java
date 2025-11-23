package com.ruleengine.core.validation;

import com.ruleengine.core.model.RuleEngineConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Composite validator that runs multiple validators in sequence.
 * Can be configured to short-circuit on critical errors.
 */
public class CompositeValidator implements ConfigValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CompositeValidator.class);
    
    private final List<ConfigValidator> validators = new ArrayList<>();
    private final boolean shortCircuitOnError;
    
    /**
     * Create a composite validator that continues validation even if errors are found.
     */
    public CompositeValidator() {
        this(false);
    }
    
    /**
     * Create a composite validator.
     * 
     * @param shortCircuitOnError If true, stop validation after first validator that produces errors
     */
    public CompositeValidator(boolean shortCircuitOnError) {
        this.shortCircuitOnError = shortCircuitOnError;
    }
    
    /**
     * Add a validator to the chain.
     */
    public CompositeValidator addValidator(ConfigValidator validator) {
        if (validator != null) {
            validators.add(validator);
        }
        return this;
    }
    
    /**
     * Add multiple validators at once.
     */
    public CompositeValidator addValidators(ConfigValidator... validators) {
        if (validators != null) {
            this.validators.addAll(Arrays.asList(validators));
        }
        return this;
    }
    
    /**
     * Add multiple validators from a list.
     */
    public CompositeValidator addValidators(List<ConfigValidator> validators) {
        if (validators != null) {
            this.validators.addAll(validators);
        }
        return this;
    }
    
    @Override
    public ValidationResult validate(RuleEngineConfig config) {
        ValidationResult result = new ValidationResult();
        
        if (validators.isEmpty()) {
            logger.warn("No validators configured in composite validator");
            result.addWarning("COMP-001", "No validators configured");
            return result;
        }
        
        logger.info("Running {} validators", validators.size());
        
        for (ConfigValidator validator : validators) {
            String validatorName = validator.getValidatorName();
            logger.debug("Running validator: {}", validatorName);
            
            try {
                ValidationResult validatorResult = validator.validate(config);
                
                logger.debug("Validator {} completed: errors={}, warnings={}", 
                            validatorName, 
                            validatorResult.getErrorCount(), 
                            validatorResult.getWarningCount());
                
                result.merge(validatorResult);
                
                // Short-circuit if enabled and errors were found
                if (shortCircuitOnError && validatorResult.hasCriticalErrors()) {
                    logger.info("Short-circuiting validation after {} due to critical errors", 
                               validatorName);
                    break;
                }
                
            } catch (Exception e) {
                logger.error("Validator {} threw exception", validatorName, e);
                result.addError("COMP-002", 
                    "Validator " + validatorName + " threw exception: " + e.getMessage(),
                    "validator=" + validatorName);
                
                if (shortCircuitOnError) {
                    logger.info("Short-circuiting validation due to validator exception");
                    break;
                }
            }
        }
        
        logger.info("Validation complete: total errors={}, warnings={}", 
                   result.getErrorCount(), result.getWarningCount());
        
        return result;
    }
    
    /**
     * Get the list of configured validators.
     */
    public List<ConfigValidator> getValidators() {
        return new ArrayList<>(validators);
    }
    
    /**
     * Get the count of configured validators.
     */
    public int getValidatorCount() {
        return validators.size();
    }
    
    /**
     * Clear all validators.
     */
    public void clearValidators() {
        validators.clear();
    }
    
    /**
     * Create a default composite validator with standard validators.
     * Includes: ReferenceValidator, ReachabilityValidator, CycleDetector
     */
    public static CompositeValidator createDefault() {
        return createDefault(true);
    }
    
    /**
     * Create a default composite validator.
     * 
     * @param shortCircuitOnError Whether to stop after first error
     */
    public static CompositeValidator createDefault(boolean shortCircuitOnError) {
        CompositeValidator composite = new CompositeValidator(shortCircuitOnError);
        
        // Add validators in order of importance
        composite.addValidator(new ReferenceValidator());
        composite.addValidator(new ReachabilityValidator());
        composite.addValidator(new CycleDetector());
        
        return composite;
    }
}

