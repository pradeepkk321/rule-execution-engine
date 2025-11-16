package com.demo.ruleengine.service;

import com.demo.ruleengine.model.OrderRequest;
import com.demo.ruleengine.model.OrderResponse;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.executor.ExecutionResult;
import com.ruleengine.spring.boot.RuleEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderProcessingService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderProcessingService.class);
    private final RuleEngineService ruleEngineService;
    
    public OrderProcessingService(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }
    
    public OrderResponse processOrder(OrderRequest request) {
        
        // Create execution context
        ExecutionContext context = new ExecutionContext();
        context.setVariable("userId", request.getUserId());
        context.setVariable("amount", request.getAmount());
        context.setVariable("items", request.getItems());
        context.setVariable("verified", request.isVerified());
        
        // Execute rules
        ExecutionResult result = ruleEngineService.execute(context);
        
        // Build response
        if (result.isSuccess()) {
            return OrderResponse.builder()
                .success(true)
                .orderId(context.getVariable("orderId", String.class))
                .total(context.getVariable("total", Double.class))
                .status(context.getVariable("status", String.class))
                .message(context.getVariable("confirmationMessage", String.class))
                .executionTimeMs(result.getExecutionTimeMs())
                .build();
        } else {
            return OrderResponse.builder()
                .success(false)
                .error(result.getErrorMessage())
                .executionTimeMs(result.getExecutionTimeMs())
                .build();
        }
    }
}