package com.ruleengine.core.action;

import java.util.Optional;

/**
 * Represents the result of an action execution.
 */
public class ActionResult {
    
    private final boolean success;
    private final Object data;
    private final String errorMessage;
    private final Throwable exception;
    
    private ActionResult(boolean success, Object data, String errorMessage, Throwable exception) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }
    
    /**
     * Create a successful action result with data.
     */
    public static ActionResult success(Object data) {
        return new ActionResult(true, data, null, null);
    }
    
    /**
     * Create a successful action result without data.
     */
    public static ActionResult success() {
        return new ActionResult(true, null, null, null);
    }
    
    /**
     * Create a failed action result with error message.
     */
    public static ActionResult failure(String errorMessage) {
        return new ActionResult(false, null, errorMessage, null);
    }
    
    /**
     * Create a failed action result with exception.
     */
    public static ActionResult failure(String errorMessage, Throwable exception) {
        return new ActionResult(false, null, errorMessage, exception);
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean isFailure() {
        return !success;
    }
    
    public Object getData() {
        return data;
    }
    
    public Optional<Object> getDataOptional() {
        return Optional.ofNullable(data);
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public Optional<String> getErrorMessageOptional() {
        return Optional.ofNullable(errorMessage);
    }
    
    public Throwable getException() {
        return exception;
    }
    
    public Optional<Throwable> getExceptionOptional() {
        return Optional.ofNullable(exception);
    }
    
    @Override
    public String toString() {
        if (success) {
            return "ActionResult{success=true, data=" + data + "}";
        } else {
            return "ActionResult{success=false, errorMessage='" + errorMessage + "'}";
        }
    }
}

