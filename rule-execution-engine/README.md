# Rule Execution Engine

Core library for a flexible, extensible rule execution engine with JEXL expression support.

## Features

- **Rule-based workflow execution** with configurable transitions
- **JEXL expression evaluation** with custom utility functions
- **Pluggable action system** via SPI pattern
- **Comprehensive validation** (reference, reachability, cycle detection)
- **Execution tracking** with detailed history and metrics
- **Error handling** with configurable error rules

## Architecture

```
com.ruleengine.core
├── action/          # Action framework and built-in actions
├── config/          # Configuration loading (JSON)
├── context/         # Execution context and state management
├── executor/        # Rule execution engine
├── expression/      # JEXL expression evaluation
├── model/           # Domain models (rules, actions, transitions)
└── validation/      # Configuration validators
```

## Installation

```xml
<dependency>
    <groupId>com.pk</groupId>
    <artifactId>rule-execution-engine</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Build locally:
```bash
mvn clean install
```

## Quick Start

### 1. Define Rules (JSON)

```json
{
  "version": "1.0",
  "entryPoint": "validate-user",
  "rules": [
    {
      "ruleId": "validate-user",
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
          "targetRule": "approve",
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
```

### 2. Execute Rules

```java
// Load configuration
ConfigurationLoader loader = new ConfigurationLoader();
RuleEngineConfig config = loader.loadFromFile("rules.json");

// Build executor
RuleExecutor executor = RuleEngineBuilder.create()
    .withConfig(config)
    .withValidation(true)
    .build();

// Create context
ExecutionContext context = new ExecutionContext();
context.setVariable("age", 25);

// Execute
ExecutionResult result = executor.execute(context);

// Get results
String status = result.getContext().getVariable("status", String.class);
System.out.println("Status: " + status); // APPROVED
```

## JEXL Utility Functions

Available via `util.*` namespace:

**Math:** `roundTo()`, `abs()`, `ceil()`, `floor()`, `max()`, `min()`, `pow()`, `sqrt()`

**String:** `upper()`, `lower()`, `trim()`, `isEmpty()`, `contains()`, `startsWith()`

**Collection:** `size()`, `first()`, `last()`, `sumItems()`, `sumField()`, `avgField()`

**Date/Time:** `now()`, `today()`, `currentTimeMillis()`, `formatDate()`

**JSON:** `toJson()`, `fromJson()`, `toPrettyJson()`

**Misc:** `uuid()`, `coalesce()`, `defaultIfNull()`, `join()`, `split()`

### Examples

```java
// Math
util.roundTo(123.456, 2)  // 123.46

// String operations
util.upper('hello')  // HELLO

// Collection operations
util.sumItems(cartItems)  // Sums price * quantity
util.sumField(items, 'total')  // Sums 'total' field

// Generate UUID
util.uuid()  // "550e8400-e29b-41d4-a716-446655440000"
```

## Custom Actions

Implement `ActionProvider` interface:

```java
@Component
public class DatabaseActionProvider implements ActionProvider {
    
    @Override
    public boolean supports(String actionType) {
        return "DATABASE".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) {
        return new DatabaseAction(definition);
    }
    
    @Override
    public int getPriority() {
        return 100; // Higher than built-in
    }
}
```

Register with `ActionRegistry`:

```java
ActionRegistry registry = new ActionRegistry();
registry.registerProvider(new DatabaseActionProvider());
```

## Configuration Reference

### Rule Definition

```json
{
  "ruleId": "unique-id",
  "description": "Rule description",
  "actions": [...],
  "transitions": [...],
  "terminal": false
}
```

### Action Definition

```json
{
  "actionId": "unique-id",
  "type": "SCRIPT|API|DATABASE|...",
  "config": { /* action-specific config */ },
  "outputVariable": "variableName",
  "outputExpression": "result.data.field",
  "continueOnError": false,
  "onError": {
    "targetRule": "error-handler"
  }
}
```

### Transition Definition

```json
{
  "condition": "amount > 100",
  "targetRule": "high-value",
  "priority": 1,
  "contextTransform": {
    "newVar": "oldVar"
  }
}
```

## Global Settings

```json
{
  "globalSettings": {
    "maxExecutionDepth": 50,
    "timeout": 30000,
    "defaultErrorRule": "error-handler"
  }
}
```

## Validation

Built-in validators:
- **ReferenceValidator** - Checks all rule references exist
- **ReachabilityValidator** - Detects unreachable rules
- **CycleDetector** - Identifies infinite loops

```java
CompositeValidator validator = CompositeValidator.createDefault();
ValidationResult result = validator.validate(config);

if (result.hasErrors()) {
    System.err.println(result.getSummary());
}
```

## Dependencies

- Jackson 2.19.2 (JSON processing)
- Apache Commons JEXL 3.3 (expressions)
- SLF4J 2.0.17 (logging)
- JUnit 5.9.3 (testing)

## License

[Your License]