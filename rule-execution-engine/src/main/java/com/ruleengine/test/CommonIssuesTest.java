package com.ruleengine.test;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.introspection.JexlPermissions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for common expression evaluation issues.
 */
public class CommonIssuesTest {
    
    public static void main(String[] args) {
        System.out.println("=== Common Issues Test ===\n");
        
        // Issue 1: JEXL Permissions
        System.out.println("1. Testing JEXL Permissions:");
        testPermissions();
        
        // Issue 2: Lambda expressions in JEXL
        System.out.println("\n2. Testing Lambda Expressions:");
        testLambdas();
        
        // Issue 3: Map property access
        System.out.println("\n3. Testing Map Property Access:");
        testMapAccess();
        
        // Issue 4: Util functions
        System.out.println("\n4. Testing Util Functions:");
        testUtilFunctions();
    }
    
    private static void testPermissions() {
        try {
            // Test with UNRESTRICTED permissions
            Map<String, Object> namespaces = new HashMap<>();
            namespaces.put("util", new com.ruleengine.core.expression.JexlUtilityFunctions());
            
            JexlEngine engine = new JexlBuilder()
                .permissions(JexlPermissions.UNRESTRICTED)
                .namespaces(namespaces)
                .strict(false)
                .create();
            
            JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator(engine);
            ExecutionContext context = new ExecutionContext();
            
            Object result = evaluator.evaluate("util.uuid()", context);
            System.out.println("  ✓ UNRESTRICTED permissions work: " + (result != null));
            
        } catch (Exception e) {
            System.out.println("  ✗ Permission issue: " + e.getMessage());
        }
    }
    
    private static void testLambdas() {
        try {
            JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
            ExecutionContext context = new ExecutionContext();
            
            List<Map<String, Object>> items = List.of(
                Map.of("price", 10.0, "quantity", 2),
                Map.of("price", 20.0, "quantity", 1)
            );
            context.setVariable("items", items);
            
            // This might fail - JEXL doesn't support Java 8 lambda syntax directly
            try {
                Object result = evaluator.evaluate(
                    "items.stream().mapToDouble(item -> item.price * item.quantity).sum()",
                    context
                );
                System.out.println("  ✓ Lambda expression works: " + result);
            } catch (Exception e) {
                System.out.println("  ✗ Lambda expression failed: " + e.getMessage());
                System.out.println("  NOTE: JEXL may not support Java 8 lambda syntax");
                System.out.println("  Alternative: Use JEXL collection operations instead");
                
                // Try JEXL alternative
                try {
                    Object alt = evaluator.evaluate(
                        "items.![price * quantity].stream().mapToDouble(x -> x).sum()",
                        context
                    );
                    System.out.println("  ✓ JEXL alternative works: " + alt);
                } catch (Exception e2) {
                    System.out.println("  ✗ JEXL alternative also failed");
                }
            }
            
        } catch (Exception e) {
            System.out.println("  ✗ Setup failed: " + e.getMessage());
        }
    }
    
    private static void testMapAccess() {
        try {
            JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
            ExecutionContext context = new ExecutionContext();
            
            Map<String, Object> nested = Map.of(
                "data", Map.of(
                    "user", Map.of(
                        "id", "123",
                        "name", "John"
                    )
                )
            );
            context.setVariable("response", nested);
            
            // Test different access patterns
            System.out.println("  Testing: response.data");
            Object data = evaluator.evaluate("response.data", context);
            System.out.println("  ✓ Works: " + data);
            
            System.out.println("  Testing: response.data.user");
            Object user = evaluator.evaluate("response.data.user", context);
            System.out.println("  ✓ Works: " + user);
            
            System.out.println("  Testing: response.data.user.id");
            Object id = evaluator.evaluate("response.data.user.id", context);
            System.out.println("  ✓ Works: " + id);
            
            // Test with get() method
            System.out.println("  Testing: response.get('data')");
            Object dataGet = evaluator.evaluate("response.get('data')", context);
            System.out.println("  ✓ Works: " + dataGet);
            
        } catch (Exception e) {
            System.out.println("  ✗ Map access failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testUtilFunctions() {
        try {
            JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
            ExecutionContext context = new ExecutionContext();
            
            String[] functions = {
                "util.uuid()",
                "util.now()",
                "util.upper('test')",
                "util.roundTo(123.456, 2)",
                "util.toJson({'key': 'value'})"
            };
            
            for (String func : functions) {
                try {
                    Object result = evaluator.evaluate(func, context);
                    System.out.println("  ✓ " + func + " = " + result);
                } catch (Exception e) {
                    System.out.println("  ✗ " + func + " failed: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.out.println("  ✗ Util functions test failed: " + e.getMessage());
        }
    }
}