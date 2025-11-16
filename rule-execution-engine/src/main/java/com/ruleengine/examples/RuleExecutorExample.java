package com.ruleengine.examples;

import com.ruleengine.core.config.ConfigurationLoader;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.executor.ExecutionResult;
import com.ruleengine.core.executor.RuleEngineBuilder;
import com.ruleengine.core.executor.RuleExecutor;
import com.ruleengine.core.model.RuleEngineConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Complete example of the rule engine in action.
 */
public class RuleExecutorExample {
    
    public static void main(String[] args) throws Exception {
        
        // Create a simple rule configuration programmatically
        String configJson = createSampleConfiguration();
        
        // Load configuration
        ConfigurationLoader loader = new ConfigurationLoader();
        RuleEngineConfig config = loader.loadFromString(configJson);
        
        // Build rule executor
        RuleExecutor executor = RuleEngineBuilder.create()
                .withConfig(config)
                .withValidation(true)
                .withBuiltInActions(true)
                .build();
        
        // Example 1: Order processing with approval
        System.out.println("=== Example 1: Small Order (Auto-Approve) ===");
        executeOrderProcessing(executor, 50.0, true);
        
        System.out.println("\n=== Example 2: Large Order (Manual Review) ===");
        executeOrderProcessing(executor, 1500.0, true);
        
        System.out.println("\n=== Example 3: Unverified Customer ===");
        executeOrderProcessing(executor, 100.0, false);
    }
    
    private static void executeOrderProcessing(RuleExecutor executor, double amount, 
                                               boolean verified) throws Exception {
        
        // Create execution context
        ExecutionContext context = new ExecutionContext();
        context.setVariable("amount", amount);
        context.setVariable("verified", verified);
        context.setVariable("taxRate", 0.1);
        
        // Execute
        ExecutionResult result = executor.execute(context);
        
        // Display results
        System.out.println("Execution Status: " + (result.isSuccess() ? "SUCCESS" : "FAILURE"));
        System.out.println("Final Rule: " + result.getFinalRuleId());
        System.out.println("Execution Time: " + result.getExecutionTimeMs() + "ms");
        System.out.println("Variables:");
        context.getAllVariables().forEach((key, value) -> 
            System.out.println("  " + key + " = " + value)
        );
        System.out.println("Execution Steps: " + context.getExecutionHistory().size());
    }
    
    private static String createSampleConfiguration() {
        return """
        {
          "version": "1.0",
          "entryPoint": "validate-order",
          "globalSettings": {
            "maxExecutionDepth": 50,
            "timeout": 30000
          },
          "rules": [
            {
              "ruleId": "validate-order",
              "description": "Validate order prerequisites",
              "actions": [
                {
                  "actionId": "check-verified",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "verified == true"
                  },
                  "outputVariable": "isVerified"
                }
              ],
              "transitions": [
                {
                  "condition": "!isVerified",
                  "targetRule": "verification-required",
                  "priority": 1
                },
                {
                  "condition": "isVerified",
                  "targetRule": "calculate-totals",
                  "priority": 2
                }
              ]
            },
            {
              "ruleId": "calculate-totals",
              "description": "Calculate order totals",
              "actions": [
                {
                  "actionId": "calculate-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(amount * taxRate, 2)"
                  },
                  "outputVariable": "tax"
                },
                {
                  "actionId": "calculate-total",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo(amount + tax, 2)"
                  },
                  "outputVariable": "total"
                },
                {
                  "actionId": "generate-order-id",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'ORD-' + util.uuid().substring(0, 8).toUpperCase()"
                  },
                  "outputVariable": "orderId"
                }
              ],
              "transitions": [
                {
                  "condition": "total > 1000",
                  "targetRule": "manual-review",
                  "priority": 1
                },
                {
                  "condition": "total <= 1000",
                  "targetRule": "auto-approve",
                  "priority": 2
                }
              ]
            },
            {
              "ruleId": "auto-approve",
              "description": "Auto-approve small orders",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'APPROVED'"
                  },
                  "outputVariable": "status"
                },
                {
                  "actionId": "set-approval-type",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'AUTOMATIC'"
                  },
                  "outputVariable": "approvalType"
                }
              ]
            },
            {
              "ruleId": "manual-review",
              "description": "Route large orders to manual review",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'PENDING_REVIEW'"
                  },
                  "outputVariable": "status"
                },
                {
                  "actionId": "set-review-reason",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Order amount exceeds auto-approval threshold'"
                  },
                  "outputVariable": "reviewReason"
                }
              ]
            },
            {
              "ruleId": "verification-required",
              "description": "Customer verification needed",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'VERIFICATION_REQUIRED'"
                  },
                  "outputVariable": "status"
                },
                {
                  "actionId": "set-message",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'Please verify your account before placing orders'"
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