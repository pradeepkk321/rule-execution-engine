package com.demo.ruleengine.action;

import com.ruleengine.core.action.*;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.model.ActionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * API Action Provider that performs HTTP requests.
 * 
 * Configuration example:
 * {
 *   "actionId": "fetch-user-data",
 *   "type": "API",
 *   "config": {
 *     "url": "https://api.example.com/users/${userId}",
 *     "method": "GET",
 *     "headers": {
 *       "Authorization": "Bearer ${token}",
 *       "Content-Type": "application/json"
 *     },
 *     "body": null,
 *     "timeout": 5000
 *   },
 *   "outputVariable": "userData",
 *   "outputExpression": "result.data"
 * }
 */
@Component
public class ApiActionProvider implements ActionProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiActionProvider.class);
    private final RestTemplate restTemplate;
    
    public ApiActionProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public boolean supports(String actionType) {
        return "API".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) throws ActionCreationException {
        validateDefinition(definition);
        return new ApiAction(definition, restTemplate);
    }
    
    @Override
    public int getPriority() {
        return 100; // Higher priority than built-in actions
    }
    
    @Override
    public String getProviderName() {
        return "ApiActionProvider";
    }
    
    private void validateDefinition(ActionDefinition definition) throws ActionCreationException {
        String actionId = definition.getActionId();
        Map config = definition.getConfig();
        
        if (config == null || config.isEmpty()) {
            throw new ActionCreationException("API", actionId, 
                "Config is required for API action");
        }
        
        if (!config.containsKey("url")) {
            throw new ActionCreationException("API", actionId, 
                "URL is required in config");
        }
    }
    
    /**
     * Inner class that performs the actual HTTP request.
     */
    private static class ApiAction implements Action {
        
        private static final Logger logger = LoggerFactory.getLogger(ApiAction.class);
        
        private final String actionId;
        private final Map config;
        private final RestTemplate restTemplate;
        
        public ApiAction(ActionDefinition definition, RestTemplate restTemplate) {
            this.actionId = definition.getActionId();
            this.config = definition.getConfig();
            this.restTemplate = restTemplate;
        }
        
        @Override
        public ActionResult execute(ExecutionContext context) throws ActionException {
            try {
                // Extract configuration
                String urlTemplate = (String) config.get("url");
                String method = (String) config.getOrDefault("method", "GET");
                
                // Resolve URL with context variables
                String url = resolveTemplate(urlTemplate, context);
                logger.debug("Executing API call: {} {}", method, url);
                
                // Build headers
                HttpHeaders headers = buildHeaders(context);
                
                // Build request body
                Object body = resolveBody(context);
                
                // Create HTTP entity
                HttpEntity entity = new HttpEntity<>(body, headers);
                
                // Execute request
                HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
                ResponseEntity response = restTemplate.exchange(
                    url,
                    httpMethod,
                    entity,
                    Map.class
                );
                
                logger.debug("API call successful: status={}", response.getStatusCode());
                
                // Return response body
                return ActionResult.success(response.getBody());
                
            } catch (Exception e) {
                logger.error("API call failed for action: {}", actionId, e);
                throw new ActionException(actionId, 
                    "API call failed: " + e.getMessage(), e);
            }
        }
        
        @Override
        public String getType() {
            return "API";
        }
        
        @Override
        public String getActionId() {
            return actionId;
        }
        
        /**
         * Resolve template with context variables.
         * Supports ${variableName} syntax.
         */
        private String resolveTemplate(String template, ExecutionContext context) {
            if (template == null) {
                return null;
            }
            
            String result = template;
            for (Map.Entry entry : context.getAllVariables().entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    Object value = entry.getValue();
                    result = result.replace(placeholder, 
                        value != null ? value.toString() : "");
                }
            }
            
            return result;
        }
        
        /**
         * Build HTTP headers with variable resolution.
         */
        private HttpHeaders buildHeaders(ExecutionContext context) {
            HttpHeaders headers = new HttpHeaders();
            
            @SuppressWarnings("unchecked")
            Map<String, String> configHeaders = 
                (Map) config.get("headers");
            
            if (configHeaders != null) {
                configHeaders.forEach((key, value) -> {
                    String resolvedValue = resolveTemplate(value, context);
                    headers.add(key, resolvedValue);
                });
            }
            
            // Set default content type if not specified
            if (!headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                headers.setContentType(MediaType.APPLICATION_JSON);
            }
            
            return headers;
        }
        
        /**
         * Resolve request body from config or context.
         */
        private Object resolveBody(ExecutionContext context) {
            Object body = config.get("body");
            
            if (body == null) {
                return null;
            }
            
            // If body is a string with ${var}, resolve it
            if (body instanceof String) {
                String bodyStr = (String) body;
                if (bodyStr.startsWith("${") && bodyStr.endsWith("}")) {
                    String varName = bodyStr.substring(2, bodyStr.length() - 1);
                    return context.getVariable(varName);
                }
            }
            
            return body;
        }
    }
}