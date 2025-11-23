package com.ruleengine.core.action;

/**
 * Exception thrown when an action execution fails.
 */
public class ActionException extends Exception {
    
    private final String actionId;
    
    public ActionException(String message) {
        super(message);
        this.actionId = null;
    }
    
    public ActionException(String message, Throwable cause) {
        super(message, cause);
        this.actionId = null;
    }
    
    public ActionException(String actionId, String message) {
        super(message);
        this.actionId = actionId;
    }
    
    public ActionException(String actionId, String message, Throwable cause) {
        super(message, cause);
        this.actionId = actionId;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    @Override
    public String toString() {
        if (actionId != null) {
            return "ActionException{actionId='" + actionId + "', message='" + getMessage() + "'}";
        }
        return super.toString();
    }
}

