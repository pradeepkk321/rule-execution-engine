package com.ruleengine.core.executor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ruleengine.core.action.ActionRegistry;
import com.ruleengine.core.action.builtin.ScriptActionProvider;
import com.ruleengine.core.config.ConfigurationLoader;
import com.ruleengine.core.context.ExecutionContext;
import com.ruleengine.core.expression.JexlExpressionEvaluator;
import com.ruleengine.core.model.RuleEngineConfig;

/**
 * Test class for ExecutionTrace and Conditional Actions.
 */
class ExecutionTraceAndConditionalActionsTest {
    
    private RuleExecutor executor;
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() throws Exception {
        loader = new ConfigurationLoader();
    }
    
    @Test
    @DisplayName("Test basic execution tracing")
    void testBasicTracing() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "start",
          "rules": [
            {
              "ruleId": "start",
              "actions": [
                {
                  "actionId": "set-value",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "100"
                  },
                  "outputVariable": "value"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        
        ExecutionResult result = executor.execute(context);
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.isTracingEnabled()).isTrue();
        
        ExecutionTrace trace = context.getTrace();
        assertThat(trace).isNotNull();
        assertThat(trace.getEntryPoint()).isEqualTo("start");
        assertThat(trace.getRulesExecuted()).containsExactly("start");
        assertThat(trace.getActionsExecuted()).containsExactly("set-value");
        assertThat(trace.getDurationMs()).isGreaterThanOrEqualTo(0);
        
        System.out.println("\n=== Basic Trace Summary ===");
        System.out.println(trace.toDetailedSummary());
        System.out.println(trace.toMermaidDiagram());
    }
    
    @Test
    @DisplayName("Test conditional actions - condition met")
    void testConditionalActionConditionMet() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "check-amount",
          "rules": [
            {
              "ruleId": "check-amount",
              "actions": [
                {
                  "actionId": "apply-discount",
                  "type": "SCRIPT",
                  "condition": "amount > 100",
                  "config": {
                    "expression": "amount * 0.9"
                  },
                  "outputVariable": "discountedAmount"
                },
                {
                  "actionId": "apply-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo((discountedAmount != null ? discountedAmount : amount) * 1.1, 2)"
                  },
                  "outputVariable": "total"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        context.setVariable("amount", 150.0);
        
        ExecutionResult result = executor.execute(context);
        
        assertThat(result.isSuccess()).isTrue();
        
        // Discount should be applied (150 * 0.9 = 135)
        Double discountedAmount = context.getVariable("discountedAmount", Double.class);
        assertThat(discountedAmount).isEqualTo(135.0);
        
        // Tax on discounted amount (135 * 1.1 = 148.5)
        Double total = context.getVariable("total", Double.class);
        assertThat(total).isEqualTo(148.5);
        
        ExecutionTrace trace = context.getTrace();
        assertThat(trace.getActionsExecuted()).containsExactly("apply-discount", "apply-tax");
        
        System.out.println("\n=== Conditional Action (Met) Trace ===");
        System.out.println(trace.toDetailedSummary());
        System.out.println(trace.toMermaidDiagram());
    }
    
    @Test
    @DisplayName("Test conditional actions - condition not met")
    void testConditionalActionConditionNotMet() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "check-amount",
          "rules": [
            {
              "ruleId": "check-amount",
              "actions": [
                {
                  "actionId": "apply-discount",
                  "type": "SCRIPT",
                  "condition": "amount > 100",
                  "config": {
                    "expression": "amount * 0.9"
                  },
                  "outputVariable": "discountedAmount"
                },
                {
                  "actionId": "apply-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "util.roundTo((discountedAmount != null ? discountedAmount : amount) * 1.1, 2)"
                  },
                  "outputVariable": "total"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        context.setVariable("amount", 50.0);  // Less than 100
        
        ExecutionResult result = executor.execute(context);
        
        assertThat(result.isSuccess()).isTrue();
        
        // Discount should NOT be applied
        Double discountedAmount = context.getVariable("discountedAmount", Double.class);
        assertThat(discountedAmount).isNull();
        
        // Tax on original amount (50 * 1.1 = 55)
        Double total = context.getVariable("total", Double.class);
        assertThat(total).isEqualTo(55.0);
        
        ExecutionTrace trace = context.getTrace();
        // Only tax action should execute, not discount
        assertThat(trace.getActionsExecuted()).containsExactly("apply-tax");
        
        System.out.println("\n=== Conditional Action (Not Met) Trace ===");
        System.out.println(trace.toDetailedSummary());
        System.out.println(trace.toMermaidDiagram());
    }
    
    @Test
    @DisplayName("Test multiple conditional actions")
    void testMultipleConditionalActions() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "customer-benefits",
          "rules": [
            {
              "ruleId": "customer-benefits",
              "actions": [
                {
                  "actionId": "premium-discount",
                  "type": "SCRIPT",
                  "condition": "customerType == 'PREMIUM'",
                  "config": {
                    "expression": "0.20"
                  },
                  "outputVariable": "discountRate"
                },
                {
                  "actionId": "standard-discount",
                  "type": "SCRIPT",
                  "condition": "customerType == 'STANDARD' && amount > 100",
                  "config": {
                    "expression": "0.10"
                  },
                  "outputVariable": "discountRate"
                },
                {
                  "actionId": "free-shipping",
                  "type": "SCRIPT",
                  "condition": "amount > 50",
                  "config": {
                    "expression": "true"
                  },
                  "outputVariable": "freeShipping"
                },
                {
                  "actionId": "calculate-final",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "amount * (1 - (discountRate != null ? discountRate : 0))"
                  },
                  "outputVariable": "finalAmount"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        // Test 1: Premium customer
        ExecutionContext context1 = new ExecutionContext();
        context1.enableTracing();
        context1.setVariable("customerType", "PREMIUM");
        context1.setVariable("amount", 100.0);
        
        ExecutionResult result1 = executor.execute(context1);
        
        assertThat(result1.isSuccess()).isTrue();
        assertThat(context1.getVariable("discountRate", Double.class)).isEqualTo(0.20);
        assertThat(context1.getVariable("freeShipping", Boolean.class)).isTrue();
        assertThat(context1.getVariable("finalAmount", Double.class)).isEqualTo(80.0);
        
        ExecutionTrace trace1 = context1.getTrace();
        assertThat(trace1.getActionsExecuted()).contains("premium-discount", "free-shipping", "calculate-final");
        assertThat(trace1.getActionsExecuted()).doesNotContain("standard-discount");
        
        System.out.println("\n=== Premium Customer Trace ===");
        System.out.println(trace1.toDetailedSummary());
        System.out.println(trace1.toMermaidDiagram());
        
        // Test 2: Standard customer with high amount
        ExecutionContext context2 = new ExecutionContext();
        context2.enableTracing();
        context2.setVariable("customerType", "STANDARD");
        context2.setVariable("amount", 150.0);
        
        ExecutionResult result2 = executor.execute(context2);
        
        assertThat(result2.isSuccess()).isTrue();
        assertThat(context2.getVariable("discountRate", Double.class)).isEqualTo(0.10);
        assertThat(context2.getVariable("freeShipping", Boolean.class)).isTrue();
        assertThat(context2.getVariable("finalAmount", Double.class)).isEqualTo(135.0);
        
        ExecutionTrace trace2 = context2.getTrace();
        assertThat(trace2.getActionsExecuted()).contains("standard-discount", "free-shipping", "calculate-final");
        assertThat(trace2.getActionsExecuted()).doesNotContain("premium-discount");
        
        System.out.println("\n=== Standard Customer Trace ===");
        System.out.println(trace2.toDetailedSummary());
        System.out.println(trace2.toMermaidDiagram());
    }
    
    @Test
    @DisplayName("Test trace with multi-rule workflow")
    void testTraceWithMultipleRules() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "validate",
          "rules": [
            {
              "ruleId": "validate",
              "actions": [
                {
                  "actionId": "check-age",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "age >= 18"
                  },
                  "outputVariable": "isAdult"
                }
              ],
              "transitions": [
                {
                  "condition": "isAdult",
                  "targetRule": "process",
                  "priority": 1
                },
                {
                  "condition": "!isAdult",
                  "targetRule": "reject",
                  "priority": 2
                }
              ]
            },
            {
              "ruleId": "process",
              "actions": [
                {
                  "actionId": "generate-id",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'USER-' + util.uuid().substring(0, 8)"
                  },
                  "outputVariable": "userId"
                }
              ],
              "transitions": [
                {
                  "condition": "true",
                  "targetRule": "approve",
                  "priority": 1
                }
              ]
            },
            {
              "ruleId": "approve",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'APPROVED'"
                  },
                  "outputVariable": "status"
                }
              ]
            },
            {
              "ruleId": "reject",
              "terminal": true,
              "actions": [
                {
                  "actionId": "set-status",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "'REJECTED'"
                  },
                  "outputVariable": "status"
                }
              ]
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        context.setVariable("age", 25);
        
        ExecutionResult result = executor.execute(context);
        
        assertThat(result.isSuccess()).isTrue();
        assertThat(context.getVariable("status", String.class)).isEqualTo("APPROVED");
        
        ExecutionTrace trace = context.getTrace();
        assertThat(trace.getRulesExecuted()).containsExactly("validate", "process", "approve");
        assertThat(trace.getActionsExecuted()).containsExactly("check-age", "generate-id", "set-status");
        
        System.out.println("\n=== Multi-Rule Workflow Trace ===");
        System.out.println(trace.toDetailedSummary());
        System.out.println("\n=== Mermaid Diagram ===");
        System.out.println(trace.toMermaidDiagram());
    }
    
    @Test
    @DisplayName("Test trace metrics and performance")
    void testTraceMetrics() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "performance-test",
          "rules": [
            {
              "ruleId": "performance-test",
              "actions": [
                {
                  "actionId": "fast-action",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "1 + 1"
                  },
                  "outputVariable": "result1"
                },
                {
                  "actionId": "slow-action",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "sum = 0; for (i = 0; i < 1000; i = i + 1) { sum = sum + i } sum"
                  },
                  "outputVariable": "result2"
                },
                {
                  "actionId": "another-fast-action",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "result1 + result2"
                  },
                  "outputVariable": "total"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        
        ExecutionResult result = executor.execute(context);
        
//        assertThat(result.isSuccess()).isTrue();
        
        ExecutionTrace trace = context.getTrace();
        
        System.out.println(trace.toMermaidDiagram());
        
        Map<String, Object> metrics = trace.getMetrics();
        
        assertThat(metrics).containsKeys(
            "totalDurationMs",
            "rulesExecuted",
            "actionsExecuted",
            "stepsExecuted",
            "actionDurations",
            "totalActionTimeMs",
            "failedActions"
        );
        
        assertThat((Integer) metrics.get("actionsExecuted")).isEqualTo(3);
        assertThat((Long) metrics.get("failedActions")).isEqualTo(0L);
        
        @SuppressWarnings("unchecked")
        Map<String, Long> actionDurations = (Map<String, Long>) metrics.get("actionDurations");
        assertThat(actionDurations).containsKeys("fast-action", "slow-action", "another-fast-action");
        
        System.out.println("\n=== Performance Metrics ===");
        metrics.forEach((key, value) -> System.out.println(key + ": " + value));
    }
    
    @Test
    @DisplayName("Test trace with variable snapshots")
    void testTraceWithSnapshots() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "calculate",
          "rules": [
            {
              "ruleId": "calculate",
              "actions": [
                {
                  "actionId": "set-base",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "100"
                  },
                  "outputVariable": "base"
                },
                {
                  "actionId": "add-tax",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "base * 1.1"
                  },
                  "outputVariable": "withTax"
                },
                {
                  "actionId": "add-fee",
                  "type": "SCRIPT",
                  "config": {
                    "expression": "withTax + 5"
                  },
                  "outputVariable": "total"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        ExecutionContext context = new ExecutionContext();
        context.enableTracing();
        
        ExecutionResult result = executor.execute(context);
        
        assertThat(result.isSuccess()).isTrue();
        
        ExecutionTrace trace = context.getTrace();

        System.out.println(trace.toMermaidDiagram());
        Map<String, Map<String, Object>> snapshots = trace.getVariableSnapshots();
        
        assertThat(snapshots).containsKeys("initial-state", "final-state");
        
        Map<String, Object> initialState = snapshots.get("initial-state");
        Map<String, Object> finalState = snapshots.get("final-state");
        
        assertThat(initialState).isEmpty();
        assertThat(finalState).containsKeys("base", "withTax", "total");
        assertThat(finalState.get("base")).isEqualTo(100.0);
        assertThat(finalState.get("withTax")).isEqualTo(110.0);
        assertThat(finalState.get("total")).isEqualTo(115.0);
        
        System.out.println("\n=== Variable Snapshots ===");
        System.out.println("Initial: " + initialState);
        System.out.println("Final: " + finalState);
    }
    
    @Test
    @DisplayName("Test conditional action with complex conditions")
    void testComplexConditionalActions() throws Exception {
        String config = """
        {
          "version": "1.0",
          "entryPoint": "complex-check",
          "rules": [
            {
              "ruleId": "complex-check",
              "actions": [
                {
                  "actionId": "check-1",
                  "type": "SCRIPT",
                  "condition": "age >= 18 && verified == true",
                  "config": {
                    "expression": "'ELIGIBLE'"
                  },
                  "outputVariable": "status"
                },
                {
                  "actionId": "check-2",
                  "type": "SCRIPT",
                  "condition": "age >= 18 && verified != true",
                  "config": {
                    "expression": "'PENDING_VERIFICATION'"
                  },
                  "outputVariable": "status"
                },
                {
                  "actionId": "check-3",
                  "type": "SCRIPT",
                  "condition": "age < 18",
                  "config": {
                    "expression": "'INELIGIBLE'"
                  },
                  "outputVariable": "status"
                }
              ],
              "terminal": true
            }
          ]
        }
        """;
        
        RuleEngineConfig ruleConfig = loader.loadFromString(config);
        executor = buildExecutor(ruleConfig);
        
        // Scenario 1: Adult, verified
        ExecutionContext context1 = new ExecutionContext();
        context1.enableTracing();
        context1.setVariable("age", 25);
        context1.setVariable("verified", true);
        
        ExecutionResult result1 = executor.execute(context1);
        assertThat(result1.isSuccess()).isTrue();
        assertThat(context1.getVariable("status", String.class)).isEqualTo("ELIGIBLE");
        assertThat(context1.getTrace().getActionsExecuted()).containsExactly("check-1");
        
        // Scenario 2: Adult, not verified
        ExecutionContext context2 = new ExecutionContext();
        context2.enableTracing();
        context2.setVariable("age", 25);
        context2.setVariable("verified", false);
        
        ExecutionResult result2 = executor.execute(context2);
        assertThat(result2.isSuccess()).isTrue();
        assertThat(context2.getVariable("status", String.class)).isEqualTo("PENDING_VERIFICATION");
        assertThat(context2.getTrace().getActionsExecuted()).containsExactly("check-2");
        
        // Scenario 3: Minor
        ExecutionContext context3 = new ExecutionContext();
        context3.enableTracing();
        context3.setVariable("age", 16);
        context3.setVariable("verified", true);
        
        ExecutionResult result3 = executor.execute(context3);
        assertThat(result3.isSuccess()).isTrue();
        assertThat(context3.getVariable("status", String.class)).isEqualTo("INELIGIBLE");
        assertThat(context3.getTrace().getActionsExecuted()).containsExactly("check-3");
        
        System.out.println("\n=== Complex Conditional Tests Passed ===");
    }
    
    private RuleExecutor buildExecutor(RuleEngineConfig config) {
        JexlExpressionEvaluator evaluator = new JexlExpressionEvaluator();
        
        ActionRegistry registry = new ActionRegistry();
        registry.registerProvider(new ScriptActionProvider(evaluator));
        
        return new RuleExecutor(config, registry, evaluator);
    }
}