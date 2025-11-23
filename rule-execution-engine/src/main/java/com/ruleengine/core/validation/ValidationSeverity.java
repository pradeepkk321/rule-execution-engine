package com.ruleengine.core.validation;

/**
 * Severity levels for validation issues.
 */
public enum ValidationSeverity {
    /**
     * Error - validation failed, configuration cannot be used.
     */
    ERROR,
    
    /**
     * Warning - potential issue, but configuration can still be used.
     */
    WARNING,
    
    /**
     * Info - informational message about the configuration.
     */
    INFO
}

