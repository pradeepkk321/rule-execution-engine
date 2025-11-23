package com.ruleengine.test;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;

import java.util.List;
import java.util.Map;

/**
 * Diagnostic test to identify expression failures.
 */
public class DiagnosticTest {
    
    public static void main(String[] args) {
        JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
        ExecutionContext context = new ExecutionContext();
        
        // Setup test data similar to ComplexRuleExample
        Map<String, Object> userApiResponse = Map.of(
            "status", "success",
            "data", Map.of(
                "user", Map.of(
                    "id", "USER-001",
                    "name", "Alice Johnson",
                    "tier", "PREMIUM"
                ),
                "creditScore", 850,
                "verificationStatus", "VERIFIED"
            )
        );
        
        List<Map<String, Object>> cartItems = List.of(
            Map.of("productId", "PROD-001", "price", 1200.0, "quantity", 1),
            Map.of("productId", "PROD-002", "price", 50.0, "quantity", 2)
        );
        
        context.setVariable("userApiResponse", userApiResponse);
        context.setVariable("cartItems", cartItems);
        
        System.out.println("=== Testing Expressions ===\n");
        
        // Test 1: Simple variable access
        testExpression(evaluator, context, "userApiResponse", "Simple variable");
        
        // Test 2: Nested property access
        testExpression(evaluator, context, "userApiResponse.data", "Nested property .data");
        testExpression(evaluator, context, "userApiResponse.data.user", "Nested property .data.user");
        testExpression(evaluator, context, "userApiResponse.data.user.id", "Nested property .data.user.id");
        
        // Test 3: Test with 'result' variable (for outputExpression)
        context.setVariable("result", userApiResponse);
        testExpression(evaluator, context, "result.data.user.id", "Using 'result' variable");
        context.removeVariable("result");
        
        // Test 4: Stream operations
        testExpression(evaluator, context, 
            "cartItems.stream().mapToDouble(item -> item.price * item.quantity).sum()",
            "Stream sum calculation");
        
        // Test 5: Util functions
        testExpression(evaluator, context, "util.uuid()", "util.uuid()");
        testExpression(evaluator, context, "util.upper('test')", "util.upper()");
        testExpression(evaluator, context, "util.roundTo(123.456, 2)", "util.roundTo()");
        
        // Test 6: Complex expressions from the example
        context.setVariable("amount", 100.0);
        context.setVariable("taxRate", 0.1);
        testExpression(evaluator, context, "util.roundTo(amount * taxRate, 2)", "Tax calculation");
        
        // Test 7: Java method calls
        testExpression(evaluator, context, "userApiResponse.get('status')", "Map.get()");
        
        // Test 8: Array/List access
        testExpression(evaluator, context, "cartItems[0]", "Array access [0]");
        testExpression(evaluator, context, "cartItems[0].price", "Array access with property");
        
        // Test 9: String concatenation
        testExpression(evaluator, context, "'ORD-' + 'TEST'", "String concatenation");
        
        // Test 10: Conditional expressions
        testExpression(evaluator, context, "amount > 50 ? 'high' : 'low'", "Ternary operator");
        
        System.out.println("\n=== Summary ===");
        System.out.println("If any tests failed, check:");
        System.out.println("1. JEXL permissions are set to UNRESTRICTED");
        System.out.println("2. util namespace is properly registered");
        System.out.println("3. Variable names match exactly");
        System.out.println("4. Data types are compatible");
    }
    
    private static void testExpression(JexlExpressionEvaluator evaluator, 
                                      ExecutionContext context,
                                      String expression, 
                                      String description) {
        System.out.println("Testing: " + description);
        System.out.println("Expression: " + expression);
        
        try {
            Object result = evaluator.evaluate(expression, context);
            System.out.println("✓ SUCCESS: " + result);
            System.out.println("  Type: " + (result != null ? result.getClass().getSimpleName() : "null"));
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
            System.out.println("  Error type: " + e.getClass().getSimpleName());
            if (e.getCause() != null) {
                System.out.println("  Cause: " + e.getCause().getMessage());
            }
        }
        System.out.println();
    }
}

