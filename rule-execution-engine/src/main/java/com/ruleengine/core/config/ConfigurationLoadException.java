package com.ruleengine.core.config;

/**
 * Exception thrown when configuration loading or parsing fails.
 */
public class ConfigurationLoadException extends Exception {
    
    private final String source;
    
    public ConfigurationLoadException(String message) {
        super(message);
        this.source = null;
    }
    
    public ConfigurationLoadException(String message, Throwable cause) {
        super(message, cause);
        this.source = null;
    }
    
    public ConfigurationLoadException(String source, String message) {
        super(message);
        this.source = source;
    }
    
    public ConfigurationLoadException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }
    
    public String getSource() {
        return source;
    }
    
    @Override
    public String toString() {
        if (source != null) {
            return "ConfigurationLoadException{source='" + source + "', message='" + getMessage() + "'}";
        }
        return super.toString();
    }
}

