package com.ruleengine.core.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Container for validation results.
 * Collects errors, warnings, and information messages during validation.
 */
public class ValidationResult {
    
    private final List<ValidationIssue> issues = new ArrayList<>();
    
    /**
     * Add an error to the validation result.
     */
    public void addError(String code, String message) {
        issues.add(new ValidationIssue(ValidationSeverity.ERROR, code, message, null));
    }
    
    /**
     * Add an error with context information.
     */
    public void addError(String code, String message, String context) {
        issues.add(new ValidationIssue(ValidationSeverity.ERROR, code, message, context));
    }
    
    /**
     * Add a warning to the validation result.
     */
    public void addWarning(String code, String message) {
        issues.add(new ValidationIssue(ValidationSeverity.WARNING, code, message, null));
    }
    
    /**
     * Add a warning with context information.
     */
    public void addWarning(String code, String message, String context) {
        issues.add(new ValidationIssue(ValidationSeverity.WARNING, code, message, context));
    }
    
    /**
     * Add an info message to the validation result.
     */
    public void addInfo(String code, String message) {
        issues.add(new ValidationIssue(ValidationSeverity.INFO, code, message, null));
    }
    
    /**
     * Add an info message with context information.
     */
    public void addInfo(String code, String message, String context) {
        issues.add(new ValidationIssue(ValidationSeverity.INFO, code, message, context));
    }
    
    /**
     * Add a custom issue.
     */
    public void addIssue(ValidationIssue issue) {
        if (issue != null) {
            issues.add(issue);
        }
    }
    
    /**
     * Merge another validation result into this one.
     */
    public void merge(ValidationResult other) {
        if (other != null) {
            issues.addAll(other.issues);
        }
    }
    
    /**
     * Check if validation passed (no errors).
     */
    public boolean isValid() {
        return !hasErrors();
    }
    
    /**
     * Check if there are any errors.
     */
    public boolean hasErrors() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == ValidationSeverity.ERROR);
    }
    
    /**
     * Check if there are any warnings.
     */
    public boolean hasWarnings() {
        return issues.stream().anyMatch(issue -> issue.getSeverity() == ValidationSeverity.WARNING);
    }
    
    /**
     * Check if validation has critical errors (for short-circuit validation).
     */
    public boolean hasCriticalErrors() {
        return hasErrors(); // For now, all errors are critical
    }
    
    /**
     * Get all issues.
     */
    public List<ValidationIssue> getIssues() {
        return Collections.unmodifiableList(issues);
    }
    
    /**
     * Get only errors.
     */
    public List<ValidationIssue> getErrors() {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == ValidationSeverity.ERROR)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only warnings.
     */
    public List<ValidationIssue> getWarnings() {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == ValidationSeverity.WARNING)
                .collect(Collectors.toList());
    }
    
    /**
     * Get only info messages.
     */
    public List<ValidationIssue> getInfos() {
        return issues.stream()
                .filter(issue -> issue.getSeverity() == ValidationSeverity.INFO)
                .collect(Collectors.toList());
    }
    
    /**
     * Get count of errors.
     */
    public int getErrorCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ValidationSeverity.ERROR)
                .count();
    }
    
    /**
     * Get count of warnings.
     */
    public int getWarningCount() {
        return (int) issues.stream()
                .filter(issue -> issue.getSeverity() == ValidationSeverity.WARNING)
                .count();
    }
    
    /**
     * Get total count of all issues.
     */
    public int getTotalIssueCount() {
        return issues.size();
    }
    
    /**
     * Clear all issues.
     */
    public void clear() {
        issues.clear();
    }
    
    /**
     * Get a formatted summary of all issues.
     */
    public String getSummary() {
        if (issues.isEmpty()) {
            return "Validation passed with no issues.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("Validation Summary:\n");
        sb.append("  Errors: ").append(getErrorCount()).append("\n");
        sb.append("  Warnings: ").append(getWarningCount()).append("\n");
        sb.append("  Total Issues: ").append(getTotalIssueCount()).append("\n");
        
        if (hasErrors()) {
            sb.append("\nErrors:\n");
            getErrors().forEach(error -> sb.append("  - ").append(error).append("\n"));
        }
        
        if (hasWarnings()) {
            sb.append("\nWarnings:\n");
            getWarnings().forEach(warning -> sb.append("  - ").append(warning).append("\n"));
        }
        
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return "ValidationResult{" +
                "errors=" + getErrorCount() +
                ", warnings=" + getWarningCount() +
                ", total=" + getTotalIssueCount() +
                ", valid=" + isValid() +
                '}';
    }
}