package com.demo.ruleengine.controller;

import com.demo.ruleengine.model.OrderRequest;
import com.demo.ruleengine.model.OrderResponse;
import com.demo.ruleengine.service.OrderProcessingService;
import jakarta.validation.Valid;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderProcessingService orderService;
    
    public OrderController(OrderProcessingService orderService) {
        this.orderService = orderService;
    }
    
    /**
     * Process an order using the rule engine.
     * 
     * POST /api/orders
     * {
     *   "userId": "USER-123",
     *   "amount": 150.00,
     *   "items": [
     *     {"productId": "PROD-1", "price": 100.0, "quantity": 1},
     *     {"productId": "PROD-2", "price": 50.0, "quantity": 1}
     *   ],
     *   "verified": true
     * }
     */
    @PostMapping
    public ResponseEntity processOrder(
            @Valid @RequestBody OrderRequest request) {
        
        logger.info("Processing order for user: {}", request.getUserId());
        
        OrderResponse response = orderService.processOrder(request);
        
        if (response.isSuccess()) {
            logger.info("Order processed successfully: orderId={}", 
                response.getOrderId());
            return ResponseEntity.ok(response);
        } else {
            logger.error("Order processing failed: {}", response.getError());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get order processing health check.
     */
    @GetMapping("/health")
    public ResponseEntity<Map> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "order-processing",
            "timestamp", java.time.Instant.now()
        ));
    }
}