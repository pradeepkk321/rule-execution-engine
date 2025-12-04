package com.ruleengine.examples;

import com.ruleengine.core.action.Action;
import com.ruleengine.core.action.ActionRegistry;
import com.ruleengine.core.action.ActionResult;
import com.ruleengine.core.action.builtin.ScriptActionProvider;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;
import com.ruleengine.core.model.ActionDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating ScriptAction usage.
 */
public class ScriptActionExample {
    
    public static void main(String[] args) throws Exception {
        
        // ==========================================
        // Example 1: Simple Calculation
        // ==========================================
        System.out.println("=== Example 1: Simple Calculation ===");
        
        // Create expression evaluator
        JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
        
        // Create and register script action provider
        ActionRegistry registry = new ActionRegistry();
        registry.registerProvider(new ScriptActionProvider(evaluator));
        
        // Create action definition
        ActionDefinition calcDef = new ActionDefinition();
        calcDef.setActionId("calculate-total");
        calcDef.setType("SCRIPT");
        
        Map<String, Object> config = new HashMap<>();
        config.put("expression", "util.roundTo(amount * 1.1 + fee, 2)");
        calcDef.setConfig(config);
        
        // Create action
        Action calcAction = registry.createAction(calcDef);
        
        // Prepare context
        ExecutionContext context = new ExecutionContext();
        context.setVariable("amount", 100.1234);
        context.setVariable("fee", 5.2358);
        
        // Execute
        ActionResult result = calcAction.execute(context);
        System.out.println("Result: " + result.getData()); // 115.0
        
        
        // ==========================================
        // Example 2: Conditional Logic
        // ==========================================
        System.out.println("\n=== Example 2: Conditional Logic ===");
        
        ActionDefinition condDef = new ActionDefinition();
        condDef.setActionId("check-eligibility");
        condDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "age >= 18 && verified ? 'ELIGIBLE' : 'NOT_ELIGIBLE'");
        condDef.setConfig(config);
        
        Action condAction = registry.createAction(condDef);
        
        context = new ExecutionContext();
        context.setVariable("age", 25);
        context.setVariable("verified", true);
        
        result = condAction.execute(context);
        System.out.println("Eligibility: " + result.getData()); // ELIGIBLE
        
        
        // ==========================================
        // Example 3: Using Utility Functions
        // ==========================================
        System.out.println("\n=== Example 3: Using Utility Functions ===");
        
        ActionDefinition utilDef = new ActionDefinition();
        utilDef.setActionId("generate-id");
        utilDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "util.uuid()");
        utilDef.setConfig(config);
        
        Action utilAction = registry.createAction(utilDef);
        
        context = new ExecutionContext();
        result = utilAction.execute(context);
        System.out.println("Generated UUID: " + result.getData());
        
        
        // ==========================================
        // Example 4: String Operations
        // ==========================================
        System.out.println("\n=== Example 4: String Operations ===");
        
        ActionDefinition strDef = new ActionDefinition();
        strDef.setActionId("format-name");
        strDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "util.upper(firstName) + ' ' + util.upper(lastName)");
        strDef.setConfig(config);
        
        Action strAction = registry.createAction(strDef);
        
        context = new ExecutionContext();
        context.setVariable("firstName", "john");
        context.setVariable("lastName", "doe");
        
        result = strAction.execute(context);
        System.out.println("Formatted Name: " + result.getData()); // JOHN DOE
        
        
        // ==========================================
        // Example 5: Working with Collections
        // ==========================================
        System.out.println("\n=== Example 5: Working with Collections ===");
        
        ActionDefinition collDef = new ActionDefinition();
        collDef.setActionId("get-first-user");
        collDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "util.first(users).name");
        collDef.setConfig(config);
        
        Action collAction = registry.createAction(collDef);
        
        context = new ExecutionContext();
        context.setVariable("users", java.util.List.of(
            Map.of("id", "1", "name", "Alice"),
            Map.of("id", "2", "name", "Bob")
        ));
        
        result = collAction.execute(context);
        System.out.println("First User Name: " + result.getData()); // Alice
        
        
        // ==========================================
        // Example 6: Complex Data Transformation
        // ==========================================
        System.out.println("\n=== Example 6: Complex Data Transformation ===");
        
        ActionDefinition transDef = new ActionDefinition();
        transDef.setActionId("calculate-discount");
        transDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", 
            "customer.premium ? price * 0.81 : (price > 100 ? price * 0.93 : price)");
        transDef.setConfig(config);
        
        Action transAction = registry.createAction(transDef);
        
        // Premium customer
        context = new ExecutionContext();
        context.setVariable("customer", Map.of("premium", true));
        context.setVariable("price", 100.0);
        
        result = transAction.execute(context);
        System.out.println("Premium Customer Price: " + result.getData()); // 80.0
        
        // Regular customer with high price
        context = new ExecutionContext();
        context.setVariable("customer", Map.of("premium", false));
        context.setVariable("price", 150.0);
        
        result = transAction.execute(context);
        System.out.println("Regular Customer Price (>100): " + result.getData()); // 135.0
        
        // Regular customer with low price
        context = new ExecutionContext();
        context.setVariable("customer", Map.of("premium", false));
        context.setVariable("price", 50.0);
        
        result = transAction.execute(context);
        System.out.println("Regular Customer Price (<100): " + result.getData()); // 50.0
        
        
        // ==========================================
        // Example 7: Date Operations
        // ==========================================
        System.out.println("\n=== Example 7: Date Operations ===");
        
        ActionDefinition dateDef = new ActionDefinition();
        dateDef.setActionId("format-timestamp");
        dateDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "util.formatDate(util.now(), 'yyyy-MM-dd HH:mm:ss')");
        dateDef.setConfig(config);
        
        Action dateAction = registry.createAction(dateDef);
        
        context = new ExecutionContext();
        result = dateAction.execute(context);
        System.out.println("Current Timestamp: " + result.getData());
        
        
        // ==========================================
        // Example 8: JSON Operations
        // ==========================================
        System.out.println("\n=== Example 8: JSON Operations ===");
        
        ActionDefinition jsonDef = new ActionDefinition();
        jsonDef.setActionId("to-json");
        jsonDef.setType("SCRIPT");
        
        config = new HashMap<>();
        config.put("expression", "util.toJson(user)");
        jsonDef.setConfig(config);
        
        Action jsonAction = registry.createAction(jsonDef);
        
        context = new ExecutionContext();
        context.setVariable("user", Map.of(
            "id", "123",
            "name", "Alice",
            "email", "alice@example.com"
        ));
        
        result = jsonAction.execute(context);
        System.out.println("User JSON: " + result.getData());
    }
}

