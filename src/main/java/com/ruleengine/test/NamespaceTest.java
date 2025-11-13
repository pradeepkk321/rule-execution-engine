package com.ruleengine.test;

import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;
import com.ruleengine.core.expression.JexlUtilityFunctions;
import org.apache.commons.jexl3.*;

import java.util.Map;

/**
 * Quick test to verify namespace functions work.
 */
public class NamespaceTest {
    
    public static void main(String[] args) throws Exception {
        
        // Test 1: Direct JEXL test with util as variable
        System.out.println("=== Test 1: Direct JEXL with UNRESTRICTED permissions ===");
        JexlEngine engine = new JexlBuilder()
            .strict(false)
            .permissions(org.apache.commons.jexl3.introspection.JexlPermissions.UNRESTRICTED)
            .create();
        MapContext jexlContext = new MapContext();
        jexlContext.set("util", new JexlUtilityFunctions());
        
        JexlExpression expr = engine.createExpression("util.uuid()");
        Object result = expr.evaluate(jexlContext);
        System.out.println("Direct JEXL util.uuid() result: " + result);
        System.out.println("Result type: " + (result != null ? result.getClass().getName() : "null"));
        
        // Test 1b: Test a simpler function
        expr = engine.createExpression("util.upper('hello')");
        result = expr.evaluate(jexlContext);
        System.out.println("Direct JEXL util.upper('hello') result: " + result);
        
        // Test 1c: Check if util is accessible
        expr = engine.createExpression("util");
        result = expr.evaluate(jexlContext);
        System.out.println("Direct JEXL util object: " + result);
        System.out.println("util class: " + (result != null ? result.getClass().getName() : "null"));
        
        // Test 2: Via our evaluator
        System.out.println("\n=== Test 2: Via JexlExpressionEvaluator ===");
        JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
        ExecutionContext execContext = new ExecutionContext();
        
        // First check if util is there
        try {
            Object utilObj = evaluator.evaluate("util", execContext);
            System.out.println("util object via evaluator: " + utilObj);
            System.out.println("util class: " + (utilObj != null ? utilObj.getClass().getName() : "null"));
        } catch (Exception e) {
            System.err.println("✗ Can't access util: " + e.getMessage());
        }
        
        // Now try calling a function
        try {
            Object uuid = evaluator.evaluate("util.uuid()", execContext);
            System.out.println("✓ util.uuid() = " + uuid);
        } catch (Exception e) {
            System.err.println("✗ util.uuid() failed: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Test a string function
        try {
            Object upper = evaluator.evaluate("util.upper('test')", execContext);
            System.out.println("✓ util.upper('test') = " + upper);
        } catch (Exception e) {
            System.err.println("✗ util.upper() failed: " + e.getMessage());
        }
        
        // Test 3: Try with colon syntax (namespace)
        System.out.println("\n=== Test 3: Colon syntax (namespace) ===");
        try {
            Object uuid = evaluator.evaluate("util:uuid()", execContext);
            System.out.println("✓ util:uuid() = " + uuid);
        } catch (Exception e) {
            System.err.println("✗ util:uuid() with colon failed: " + e.getMessage());
        }
    }
}