package com.ruleengine.spring.examples;

import com.ruleengine.core.action.*;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.model.ActionDefinition;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Example API action using Spring RestTemplate.
 * 
 * Users can copy this class and customize it for their needs.
 * 
 * Configuration example:
 * {
 *   "actionId": "call-user-api",
 *   "type": "API",
 *   "config": {
 *     "url": "https://api.example.com/users/123",
 *     "method": "GET",
 *     "headers": {
 *       "Authorization": "Bearer ${token}"
 *     }
 *   },
 *   "outputVariable": "userResponse"
 * }
 */
@Component
public class SpringApiActionExample implements ActionProvider {
    
    private final RestTemplate restTemplate;
    
    public SpringApiActionExample(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public boolean supports(String actionType) {
        return "API".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) throws ActionCreationException {
        return new ApiAction(definition, restTemplate);
    }
    
    @Override
    public int getPriority() {
        return 100; // Higher than built-in actions
    }
    
    /**
     * Inner action class that performs the HTTP call.
     */
    private static class ApiAction implements Action {
        
        private final String actionId;
        private final String url;
        private final String method;
        private final Map<String, Object> config;
        private final RestTemplate restTemplate;
        
        public ApiAction(ActionDefinition definition, RestTemplate restTemplate) {
            this.actionId = definition.getActionId();
            this.config = definition.getConfig();
            this.url = (String) config.get("url");
            this.method = (String) config.getOrDefault("method", "GET");
            this.restTemplate = restTemplate;
        }
        
        @Override
        public ActionResult execute(ExecutionContext context) throws ActionException {
            try {
                // Resolve URL with context variables (simple template)
                String resolvedUrl = resolveTemplate(url, context);
                
                // Build headers
                HttpHeaders headers = new HttpHeaders();
                @SuppressWarnings("unchecked")
                Map<String, String> configHeaders = 
                    (Map<String, String>) config.get("headers");
                
                if (configHeaders != null) {
                    configHeaders.forEach((key, value) -> {
                        String resolvedValue = resolveTemplate(value, context);
                        headers.add(key, resolvedValue);
                    });
                }
                
                // Build request
                Object body = config.get("body");
                HttpEntity<?> entity = new HttpEntity<>(body, headers);
                
                // Execute HTTP call
                ResponseEntity<Map> response;
                HttpMethod httpMethod = HttpMethod.valueOf(method.toUpperCase());
                
                response = restTemplate.exchange(
                    resolvedUrl, 
                    httpMethod, 
                    entity, 
                    Map.class
                );
                
                return ActionResult.success(response.getBody());
                
            } catch (Exception e) {
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
         * Simple template resolution: ${variableName}
         */
        private String resolveTemplate(String template, ExecutionContext context) {
            if (template == null) {
                return null;
            }
            
            String result = template;
            
            // Simple ${var} replacement
            for (Map.Entry<String, Object> entry : context.getAllVariables().entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                if (result.contains(placeholder)) {
                    Object value = entry.getValue();
                    result = result.replace(placeholder, 
                        value != null ? value.toString() : "");
                }
            }
            
            return result;
        }
    }
}