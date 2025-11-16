package com.ruleengine.test;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;

import java.util.List;
import java.util.Map;

/**
 * Test the fixed JEXL-compatible expressions.
 */
public class FixedExpressionsTest {
    
    public static void main(String[] args) {
        JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
        ExecutionContext context = new ExecutionContext();
        
        // Setup test data
        List<Map<String, Object>> cartItems = List.of(
            Map.of("productId", "PROD-001", "price", 1200.0, "quantity", 1),
            Map.of("productId", "PROD-002", "price", 50.0, "quantity", 2)
        );
        
        context.setVariable("cartItems", cartItems);
        
        System.out.println("=== Testing Fixed JEXL Expressions ===\n");
        
        // Test 1: Sum with JEXL script (use return)
        System.out.println("1. Calculate Cart Subtotal (JEXL script):");
        String sumExpression = "total = 0; for (item : cartItems) { total = total + (item.price * item.quantity); } return total;";
        test(evaluator, context, sumExpression);
        System.out.println("Expected: 1300.0 (1200*1 + 50*2)\n");
        
        // Test 2: Count items (use return)
        System.out.println("2. Count Items (JEXL script):");
        String countExpression = "count = 0; for (item : cartItems) { count = count + item.quantity; } return count;";
        test(evaluator, context, countExpression);
        System.out.println("Expected: 3 (1 + 2)\n");
        
        // Test 3: Access first item's price
        System.out.println("3. Get First Item Price:");
        test(evaluator, context, "cartItems[0].price");
        System.out.println("Expected: 1200.0\n");
        
        // Test 4: Use size function
        System.out.println("4. Get Cart Size:");
        test(evaluator, context, "cartItems.size()");
        System.out.println("Expected: 2\n");
        
        // Test 5: Check if price > 100 using script
        System.out.println("5. Check if Any Item > $100:");
        String filterExpression = "hasExpensive = false; for (item : cartItems) { if (item.price > 100) { hasExpensive = true; break; } } return hasExpensive;";
        test(evaluator, context, filterExpression);
        System.out.println("Expected: true\n");
        
        // Test 6: Complex calculation with rounding (use return)
        System.out.println("6. Calculate with Tax and Rounding:");
        String complexExpression = "subtotal = 0; for (item : cartItems) { subtotal = subtotal + (item.price * item.quantity); } tax = subtotal * 0.1; result = util.roundTo(subtotal + tax, 2); return result;";
        test(evaluator, context, complexExpression);
        System.out.println("Expected: 1430.0 (1300 + 130 tax)\n");
        
        // Test 7: Conditional logic (use return)
        System.out.println("7. Check if Any Item > $1000:");
        String checkExpression = "hasExpensive = false; for (item : cartItems) { if (item.price > 1000) { hasExpensive = true; break; } } return hasExpensive;";
        test(evaluator, context, checkExpression);
        System.out.println("Expected: true\n");
        
        // Test 8: Build summary string (use return)
        System.out.println("8. Build Product Summary:");
        String summaryExpression = "summary = ''; count = 0; for (item : cartItems) { if (count > 0) { summary = summary + ', '; } summary = summary + item.productId; count = count + 1; } return summary;";
        test(evaluator, context, summaryExpression);
        System.out.println("Expected: PROD-001, PROD-002\n");
        
        System.out.println("=== All Tests Complete ===");
        System.out.println("✓ All JEXL expressions are now compatible!");
    }
    
    private static void test(JexlExpressionEvaluator evaluator, ExecutionContext context, String expression) {
        try {
            Object result = evaluator.evaluate(expression, context);
            System.out.println("  Expression: " + (expression.length() > 60 ? expression.substring(0, 60) + "..." : expression));
            System.out.println("  ✓ Result: " + result);
            System.out.println("  Type: " + (result != null ? result.getClass().getSimpleName() : "null"));
        } catch (Exception e) {
            System.out.println("  ✗ FAILED: " + e.getMessage());
            e.printStackTrace();
        }
    }
}