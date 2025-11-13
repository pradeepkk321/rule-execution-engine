package com.ruleengine.core.action;

/**
 * Exception thrown when an action cannot be created from its definition.
 */
public class ActionCreationException extends Exception {
    
    private final String actionType;
    private final String actionId;
    
    public ActionCreationException(String message) {
        super(message);
        this.actionType = null;
        this.actionId = null;
    }
    
    public ActionCreationException(String message, Throwable cause) {
        super(message, cause);
        this.actionType = null;
        this.actionId = null;
    }
    
    public ActionCreationException(String actionType, String actionId, String message) {
        super(message);
        this.actionType = actionType;
        this.actionId = actionId;
    }
    
    public ActionCreationException(String actionType, String actionId, String message, Throwable cause) {
        super(message, cause);
        this.actionType = actionType;
        this.actionId = actionId;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public String getActionId() {
        return actionId;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ActionCreationException{");
        if (actionType != null) {
            sb.append("actionType='").append(actionType).append('\'');
        }
        if (actionId != null) {
            sb.append(", actionId='").append(actionId).append('\'');
        }
        sb.append(", message='").append(getMessage()).append('\'');
        sb.append('}');
        return sb.toString();
    }
}