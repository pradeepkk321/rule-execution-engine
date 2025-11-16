package com.example.ruleengine;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.executor.ExecutionResult;
import com.ruleengine.spring.boot.RuleEngineService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Example Spring Boot application using the rule engine.
 */
@SpringBootApplication
public class RuleEngineApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(RuleEngineApplication.class, args);
    }
    
    /**
     * Provide RestTemplate bean for API actions.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

/**
 * Example REST controller that uses the rule engine.
 */
@RestController
@RequestMapping("/api")
class OrderController {
    
    private final RuleEngineService ruleEngineService;
    
    public OrderController(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }
    
    /**
     * Process an order using the rule engine.
     * 
     * POST /api/orders
     * {
     *   "userId": "12345",
     *   "amount": 100.0,
     *   "items": [...]
     * }
     */
    @PostMapping("/orders")
    public OrderResponse processOrder(@RequestBody OrderRequest request) {
        
        // Create execution context with request data
        ExecutionContext context = new ExecutionContext();
        context.setVariable("userId", request.getUserId());
        context.setVariable("amount", request.getAmount());
        context.setVariable("items", request.getItems());
        
        // Execute rules
        ExecutionResult result = ruleEngineService.execute(context);
        
        // Build response
        if (result.isSuccess()) {
            return OrderResponse.success(
                context.getVariable("orderId", String.class),
                context.getVariable("total", Double.class),
                context.getVariable("status", String.class),
                result.getExecutionTimeMs()
            );
        } else {
            return OrderResponse.failure(result.getErrorMessage());
        }
    }
    
    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "ruleEngine", "active",
            "rules", ruleEngineService.getRuleExecutor().getConfig().getRuleCount()
        );
    }
}

/**
 * Order request DTO.
 */
class OrderRequest {
    private String userId;
    private Double amount;
    private java.util.List<Map<String, Object>> items;
    
    // Getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    
    public java.util.List<Map<String, Object>> getItems() { return items; }
    public void setItems(java.util.List<Map<String, Object>> items) { this.items = items; }
}

/**
 * Order response DTO.
 */
class OrderResponse {
    private boolean success;
    private String orderId;
    private Double total;
    private String status;
    private String error;
    private Long executionTimeMs;
    
    public static OrderResponse success(String orderId, Double total, 
                                       String status, Long executionTimeMs) {
        OrderResponse response = new OrderResponse();
        response.success = true;
        response.orderId = orderId;
        response.total = total;
        response.status = status;
        response.executionTimeMs = executionTimeMs;
        return response;
    }
    
    public static OrderResponse failure(String error) {
        OrderResponse response = new OrderResponse();
        response.success = false;
        response.error = error;
        return response;
    }
    
    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { 
        this.executionTimeMs = executionTimeMs; 
    }
}