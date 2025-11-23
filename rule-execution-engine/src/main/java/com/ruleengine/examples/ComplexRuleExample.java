package com.ruleengine.examples;

import com.ruleengine.core.config.ConfigurationLoader;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.executor.ExecutionResult;
import com.ruleengine.core.executor.RuleEngineBuilder;
import com.ruleengine.core.executor.RuleExecutor;
import com.ruleengine.core.model.RuleEngineConfig;

import java.util.List;
import java.util.Map;

/**
 * Complex example demonstrating:
 * 1. outputExpression - Extracting nested data from action results
 * 2. contextTransform - Renaming variables between rules
 * 
 * Scenario: E-commerce order processing with user validation, 
 * discount calculation, and inventory check.
 */
public class ComplexRuleExample {
    
    public static void main(String[] args) throws Exception {
        
        String configJson = createComplexConfiguration();
        
        // Load and build
        ConfigurationLoader loader = new ConfigurationLoader();
        RuleEngineConfig config = loader.loadFromString(configJson);
        
        RuleExecutor executor = RuleEngineBuilder.create()
                .withConfig(config)
                .withValidation(true)
                .build();
        
        // Example 1: Premium customer with high-value items
        System.out.println("=== Example 1: Premium Customer ===");
        executePremiumOrder(executor);
        
        // Example 2: Regular customer with standard items
        System.out.println("\n=== Example 2: Regular Customer ===");
        executeRegularOrder(executor);
        
        // Example 3: New customer with multiple items
        System.out.println("\n=== Example 3: New Customer ===");
        executeNewCustomerOrder(executor);
    }
    
    private static void executePremiumOrder(RuleExecutor executor) throws Exception {
        ExecutionContext context = new ExecutionContext();
        
        // Simulate API response with nested data
        Map<String, Object> userApiResponse = Map.of(
            "status", "success",
            "data", Map.of(
                "user", Map.of(
                    "id", "USER-001",
                    "name", "Alice Johnson",
                    "email", "alice@example.com",
                    "tier", "PREMIUM",
                    "memberSince", "2020-01-15"
                ),
                "creditScore", 850,
                "verificationStatus", "VERIFIED"
            ),
            "timestamp", "2024-01-15T10:30:00Z"
        );
        
        // Simulate cart items
        List<Map<String, Object>> cartItems = List.of(
            Map.of("productId", "PROD-001", "name", "Laptop", "price", 1200.0, "quantity", 1),
            Map.of("productId", "PROD-002", "name", "Mouse", "price", 50.0, "quantity", 2)
        );
        
        context.setVariable("userApiResponse", userApiResponse);
        context.setVariable("cartItems", cartItems);
        
        ExecutionResult result = executor.execute(context);
        
        printResult(result, context);
    }
    
    private static void executeRegularOrder(RuleExecutor executor) throws Exception {
        ExecutionContext context = new ExecutionContext();
        
        Map<String, Object> userApiResponse = Map.of(
            "status", "success",
            "data", Map.of(
                "user", Map.of(
                    "id", "USER-002",
                    "name", "Bob Smith",
                    "email", "bob@example.com",
                    "tier", "STANDARD",
                    "memberSince", "2023-06-20"
                ),
                "creditScore", 720,
                "verificationStatus", "VERIFIED"
            )
        );
        
        List<Map<String, Object>> cartItems = List.of(
            Map.of("productId", "PROD-003", "name", "Book", "price", 25.0, "quantity", 3)
        );
        
        context.setVariable("userApiResponse", userApiResponse);
        context.setVariable("cartItems", cartItems);
        
        ExecutionResult result = executor.execute(context);
        
        printResult(result, context);
    }
    
    private static void executeNewCustomerOrder(RuleExecutor executor) throws Exception {
        ExecutionContext context = new ExecutionContext();
        
        Map<String, Object> userApiResponse = Map.of(
            "status", "success",
            "data", Map.of(
                "user", Map.of(
                    "id", "USER-003",
                    "name", "Charlie Davis",
                    "email", "charlie@example.com",
                    "tier", "NEW",
                    "memberSince", "2024-01-01"
                ),
                "creditScore", 680,
                "verificationStatus", "PENDING"
            )
        );
        
        List<Map<String, Object>> cartItems = List.of(
            Map.of("productId", "PROD-004", "name", "Phone", "price", 800.0, "quantity", 1)
        );
        
        context.setVariable("userApiResponse", userApiResponse);
        context.setVariable("cartItems", cartItems);
        
        ExecutionResult result = executor.execute(context);
        
        printResult(result, context);
    }
    
    private static void printResult(ExecutionResult result, ExecutionContext context) {
        System.out.println("Status: " + (result.isSuccess() ? "SUCCESS" : "FAILURE"));
        System.out.println("Final Rule: " + result.getFinalRuleId());
        System.out.println("Execution Time: " + result.getExecutionTimeMs() + "ms");
        System.out.println("\nKey Variables:");
        
        // Print relevant variables
        String[] keys = {"userId", "userName", "userTier", "subtotal", "discount", 
                        "tax", "total", "orderStatus", "discountReason"};
        
        for (String key : keys) {
            Object value = context.getVariable(key);
            if (value != null) {
                System.out.println("  " + key + " = " + value);
            }
        }
        
        System.out.println("\nExecution Steps: " + context.getExecutionHistory().size());
        
        // Show error if failed
        if (result.isFailure()) {
            System.out.println("Error: " + result.getErrorMessage());
            if (context.hasError()) {
                System.out.println("Error Details: " + context.getError());
            }
        }
    }
    
    private static String createComplexConfiguration() {
        return """
        {
          "version": "1.0",
          "entryPoint": "fetch-user-data",
          "globalSettings": {
            "maxExecutionDepth": 50
          },
          "rules": [
            {
              "ruleId": "fetch-user-data",
              "description": "Extract user data from API response using outputExpression",
              "actions": [
                {
                  "actionId": "extract-user-id",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "userApiResponse"
                  },
                  "outputVariable": "userId",
                  "outputExpression": "result.data.user.id"
                },
                {
                  "actionId": "extract-user-name",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "userApiResponse"
                  },
                  "outputVariable": "userName",
                  "outputExpression": "result.data.user.name"
                },
                {
                  "actionId": "extract-user-tier",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "userApiResponse"
                  },
                  "outputVariable": "userTier",
                  "outputExpression": "result.data.user.tier"
                },
                {
                  "actionId": "extract-credit-score",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "userApiResponse"
                  },
                  "outputVariable": "creditScore",
                  "outputExpression": "result.data.creditScore"
                },
                {
                  "actionId": "extract-verification",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "userApiResponse"
                  },
                  "outputVariable": "verificationStatus",
                  "outputExpression": "result.data.verificationStatus"
                }
              ],
              "transitions": [
                {
                  "condition": "verificationStatus == 'PENDING'",
                  "targetRule": "verification-required",
                  "priority": 1
                },
                {
                  "condition": "verificationStatus == 'VERIFIED'",
                  "targetRule": "calculate-cart-total",
                  "priority": 2,
                  "contextTransform": {
                    "user_id": "userId",
                    "user_name": "userName",
                    "user_tier": "userTier",
                    "credit_score": "creditScore"
                  }
                }
              ]
            },
            {
              "ruleId": "calculate-cart-total",
              "description": "Calculate subtotal from cart items",
              "actions": [
                {
                  "actionId": "sum-cart-items",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.sumItems(cartItems)"
                  },
                  "outputVariable": "subtotal"
                },
                {
                  "actionId": "count-items",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "count = 0; for (item : cartItems) { count = count + item.quantity; } return count;"
                  },
                  "outputVariable": "itemCount"
                }
              ],
              "transitions": [
                {
                  "condition": "user_tier == 'PREMIUM'",
                  "targetRule": "apply-premium-discount",
                  "priority": 1,
                  "contextTransform": {
                    "customerId": "user_id",
                    "customerName": "user_name",
                    "orderSubtotal": "subtotal"
                  }
                },
                {
                  "condition": "user_tier == 'STANDARD' && subtotal > 100",
                  "targetRule": "apply-standard-discount",
                  "priority": 2,
                  "contextTransform": {
                    "customerId": "user_id",
                    "customerName": "user_name",
                    "orderSubtotal": "subtotal"
                  }
                },
                {
                  "condition": "true",
                  "targetRule": "calculate-final-price",
                  "priority": 3,
                  "contextTransform": {
                    "customerId": "user_id",
                    "customerName": "user_name",
                    "orderSubtotal": "subtotal",
                    "discountAmount": "0"
                  }
                }
              ]
            },
            {
              "ruleId": "apply-premium-discount",
              "description": "Apply 20% discount for premium customers",
              "actions": [
                {
                  "actionId": "calculate-discount",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "orderSubtotal * 0.20"
                  },
                  "outputVariable": "discountAmount"
                },
                {
                  "actionId": "round-discount",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(discountAmount, 2)"
                  },
                  "outputVariable": "discountAmount"
                },
                {
                  "actionId": "set-discount-reason",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Premium member discount (20%)'"
                  },
                  "outputVariable": "discountReason"
                }
              ],
              "transitions": [
                {
                  "condition": "true",
                  "targetRule": "calculate-final-price",
                  "priority": 1
                }
              ]
            },
            {
              "ruleId": "apply-standard-discount",
              "description": "Apply 10% discount for orders over $100",
              "actions": [
                {
                  "actionId": "calculate-discount",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "orderSubtotal * 0.10"
                  },
                  "outputVariable": "discountAmount"
                },
                {
                  "actionId": "round-discount",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(discountAmount, 2)"
                  },
                  "outputVariable": "discountAmount"
                },
                {
                  "actionId": "set-discount-reason",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Order over $100 discount (10%)'"
                  },
                  "outputVariable": "discountReason"
                }
              ],
              "transitions": [
                {
                  "condition": "true",
                  "targetRule": "calculate-final-price",
                  "priority": 1
                }
              ]
            },
            {
              "ruleId": "calculate-final-price",
              "description": "Calculate final price with tax",
              "actions": [
                {
                  "actionId": "apply-discount",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "orderSubtotal - (discountAmount != null ? discountAmount : 0)"
                  },
                  "outputVariable": "discountedTotal"
                },
                {
                  "actionId": "round-discounted-total",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(discountedTotal, 2)"
                  },
                  "outputVariable": "discountedTotal"
                },
                {
                  "actionId": "calculate-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "discountedTotal * 0.08"
                  },
                  "outputVariable": "tax"
                },
                {
                  "actionId": "round-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(tax, 2)"
                  },
                  "outputVariable": "tax"
                },
                {
                  "actionId": "calculate-total",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "discountedTotal + tax"
                  },
                  "outputVariable": "total"
                },
                {
                  "actionId": "round-total",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(total, 2)"
                  },
                  "outputVariable": "total"
                },
                {
                  "actionId": "generate-order-id",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'ORD-' + customerId + '-' + util.uuid().substring(0, 8).toUpperCase()"
                  },
                  "outputVariable": "orderId"
                }
              ],
              "transitions": [
                {
                  "condition": "total > 1000",
                  "targetRule": "high-value-order",
                  "priority": 1,
                  "contextTransform": {
                    "finalOrderId": "orderId",
                    "finalTotal": "total",
                    "customer": "customerName"
                  }
                },
                {
                  "condition": "true",
                  "targetRule": "standard-order",
                  "priority": 2,
                  "contextTransform": {
                    "finalOrderId": "orderId",
                    "finalTotal": "total",
                    "customer": "customerName"
                  }
                }
              ]
            },
            {
              "ruleId": "high-value-order",
              "description": "High value order processing",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'APPROVED_HIGH_VALUE'"
                  },
                  "outputVariable": "orderStatus"
                },
                {
                  "actionId": "set-priority",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'HIGH'"
                  },
                  "outputVariable": "processingPriority"
                },
                {
                  "actionId": "set-message",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Thank you ' + customer + '! Your high-value order ' + finalOrderId + ' will be processed with priority shipping.'"
                  },
                  "outputVariable": "confirmationMessage"
                }
              ]
            },
            {
              "ruleId": "standard-order",
              "description": "Standard order processing",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'APPROVED'"
                  },
                  "outputVariable": "orderStatus"
                },
                {
                  "actionId": "set-priority",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'NORMAL'"
                  },
                  "outputVariable": "processingPriority"
                },
                {
                  "actionId": "set-message",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Thank you ' + customer + '! Your order ' + finalOrderId + ' has been confirmed.'"
                  },
                  "outputVariable": "confirmationMessage"
                }
              ]
            },
            {
              "ruleId": "verification-required",
              "description": "User verification needed",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'VERIFICATION_REQUIRED'"
                  },
                  "outputVariable": "orderStatus"
                },
                {
                  "actionId": "set-message",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Dear ' + userName + ', please verify your account before placing orders.'"
                  },
                  "outputVariable": "message"
                }
              ]
            }
          ]
        }
        """;
    }
}

