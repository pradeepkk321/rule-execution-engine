package com.ruleengine.core.validation;

/**
 * Represents a single validation issue (error, warning, or info).
 */
public class ValidationIssue {
    
    private final ValidationSeverity severity;
    private final String code;
    private final String message;
    private final String context;
    
    public ValidationIssue(ValidationSeverity severity, String code, String message, String context) {
        this.severity = severity;
        this.code = code;
        this.message = message;
        this.context = context;
    }
    
    public ValidationSeverity getSeverity() {
        return severity;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getContext() {
        return context;
    }
    
    public boolean hasContext() {
        return context != null && !context.trim().isEmpty();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(severity).append("]");
        
        if (code != null) {
            sb.append(" ").append(code);
        }
        
        sb.append(": ").append(message);
        
        if (hasContext()) {
            sb.append(" (").append(context).append(")");
        }
        
        return sb.toString();
    }
}