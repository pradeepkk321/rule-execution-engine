# Rule Engine Platform

A flexible, extensible rule execution engine for Java with Spring Boot integration. Execute complex business logic workflows using JSON-configured rules and JEXL expressions.

## Overview

This platform consists of three modules:

1. **rule-execution-engine** - Core rule engine library
2. **rule-engine-spring-boot-starter** - Spring Boot auto-configuration
3. **rule-engine-demo** - Example application with API actions

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+

### Build & Run

```bash
# Build all modules
cd rule-execution-engine
mvn clean install

cd ../rule-engine-spring-boot-starter
mvn clean install

cd ../rule-engine-demo
mvn spring-boot:run
```

### Test the Demo

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "amount": 150.0,
    "items": [
      {"productId": "PROD-1", "price": 100.0, "quantity": 1},
      {"productId": "PROD-2", "price": 50.0, "quantity": 1}
    ],
    "verified": true
  }'
```

## Key Features

### Rule Engine Core
- JSON-based rule configuration
- JEXL expression evaluation with 50+ utility functions
- Pluggable action system (SPI pattern)
- Comprehensive validation (references, reachability, cycles)
- Execution history and metrics
- Error handling with configurable error rules

### Spring Boot Integration
- Auto-configuration
- Properties-based setup
- Automatic bean registration
- Component scanning for custom actions
- Built-in example actions (API, Database)

### Demo Application
- Order processing workflow
- External API integration
- Tax/discount calculations
- Multi-step rule execution
- REST API endpoints
- Spring Boot Actuator monitoring

## Architecture

```
┌─────────────────────────────────────────┐
│     rule-engine-demo (Application)      │
│  - REST Controllers                     │
│  - Business Services                    │
│  - Custom Action Providers              │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│  rule-engine-spring-boot-starter        │
│  - Auto Configuration                   │
│  - RuleEngineService                    │
│  - Spring Integration                   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│     rule-execution-engine (Core)        │
│  - Rule Executor                        │
│  - Action Registry                      │
│  - JEXL Expression Evaluator            │
│  - Validators                           │
└─────────────────────────────────────────┘
```

## Module Details

### 1. rule-execution-engine

Core library with rule execution logic.

**Key Components:**
- `RuleExecutor` - Orchestrates rule execution
- `ActionRegistry` - Manages action providers
- `ExpressionEvaluator` - JEXL-based expression evaluation
- `ConfigurationLoader` - Loads rules from JSON
- `CompositeValidator` - Validates rule configuration

**Dependencies:**
```xml
<dependency>
    <groupId>com.pk</groupId>
    <artifactId>rule-execution-engine</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

[Full Documentation →](rule-execution-engine/README.md)

### 2. rule-engine-spring-boot-starter

Spring Boot integration layer.

**Key Components:**
- `RuleEngineAutoConfiguration` - Auto-configures beans
- `RuleEngineService` - Spring-friendly service wrapper
- `RuleEngineProperties` - Configuration properties

**Configuration:**
```yaml
rule-engine:
  config-location: classpath:rules/order-processing.json
  validate-on-startup: true
  max-execution-depth: 50
  execution-timeout: 30000
```

**Dependencies:**
```xml
<dependency>
    <groupId>com.pk</groupId>
    <artifactId>rule-engine-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

[Full Documentation →](rule-engine-spring-boot-starter/README.md)

### 3. rule-engine-demo

Example application demonstrating usage.

**Features:**
- Order processing REST API
- External API integration (JSONPlaceholder)
- JEXL calculations for tax/discounts
- Multi-step workflows
- Actuator monitoring

**Endpoints:**
- `POST /api/orders` - Process order
- `GET /api/orders/health` - Health check
- `GET /actuator/*` - Actuator endpoints

[Full Documentation →](rule-engine-demo/README.md)

## Rule Configuration Example

```json
{
  "version": "1.0",
  "entryPoint": "validate-user",
  "globalSettings": {
    "maxExecutionDepth": 50,
    "timeout": 30000
  },
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
    }
  ]
}
```

## Usage Example

```java
@Service
public class OrderService {
    
    @Autowired
    private RuleEngineService ruleEngineService;
    
    public OrderResponse processOrder(OrderRequest request) {
        // Create context
        ExecutionContext context = new ExecutionContext();
        context.setVariable("userId", request.getUserId());
        context.setVariable("amount", request.getAmount());
        
        // Execute rules
        ExecutionResult result = ruleEngineService.execute(context);
        
        // Extract results
        if (result.isSuccess()) {
            return OrderResponse.success(
                context.getVariable("orderId", String.class),
                context.getVariable("total", Double.class)
            );
        }
        return OrderResponse.failure(result.getErrorMessage());
    }
}
```

## JEXL Utility Functions

Available via `util.*` namespace:

**Math:** `roundTo()`, `abs()`, `ceil()`, `floor()`, `max()`, `min()`, `pow()`, `sqrt()`

**String:** `upper()`, `lower()`, `trim()`, `isEmpty()`, `contains()`, `startsWith()`, `endsWith()`

**Collections:** `size()`, `first()`, `last()`, `sumItems()`, `sumField()`, `avgField()`

**Date/Time:** `now()`, `today()`, `currentTimeMillis()`, `formatDate()`

**JSON:** `toJson()`, `fromJson()`, `toPrettyJson()`

**Misc:** `uuid()`, `coalesce()`, `defaultIfNull()`, `join()`, `split()`, `randomInt()`

**Examples:**
```java
util.roundTo(123.456, 2)              // 123.46
util.sumItems(cartItems)               // Sum price * quantity
util.formatDate(util.now(), 'yyyy-MM-dd')  // "2024-01-15"
util.uuid()                            // Generate UUID
```

## Creating Custom Actions

Implement `ActionProvider` interface:

```java
@Component
public class EmailActionProvider implements ActionProvider {
    
    @Override
    public boolean supports(String actionType) {
        return "EMAIL".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) {
        return new EmailAction(definition);
    }
    
    @Override
    public int getPriority() {
        return 200; // Higher = more precedence
    }
}
```

Configure in rules:
```json
{
  "actionId": "send-confirmation",
  "type": "EMAIL",
  "config": {
    "to": "${userEmail}",
    "subject": "Order Confirmation",
    "template": "order-confirmation"
  }
}
```

## Project Structure

```
.
├── rule-execution-engine/           # Core library
│   ├── src/main/java/com/ruleengine/core/
│   │   ├── action/                  # Action framework
│   │   ├── config/                  # Configuration loading
│   │   ├── context/                 # Execution context
│   │   ├── executor/                # Rule execution
│   │   ├── expression/              # JEXL evaluation
│   │   ├── model/                   # Domain models
│   │   └── validation/              # Validators
│   └── pom.xml
│
├── rule-engine-spring-boot-starter/ # Spring Boot integration
│   ├── src/main/java/com/ruleengine/spring/boot/
│   │   ├── RuleEngineAutoConfiguration.java
│   │   ├── RuleEngineProperties.java
│   │   └── RuleEngineService.java
│   ├── src/main/resources/
│   │   └── META-INF/spring.factories
│   └── pom.xml
│
├── rule-engine-demo/                # Demo application
│   ├── src/main/java/com/demo/ruleengine/
│   │   ├── action/                  # Custom actions
│   │   ├── controller/              # REST controllers
│   │   ├── model/                   # DTOs
│   │   └── service/                 # Business services
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── rules/
│   │       └── order-processing.json
│   └── pom.xml
│
└── README.md                        # This file
```

## Configuration Properties

```yaml
rule-engine:
  # Rule configuration location
  config-location: classpath:rules.json
  
  # Validation
  validate-on-startup: true
  
  # Execution limits
  max-execution-depth: 50
  execution-timeout: 30000
  
  # Expression caching
  cache-expressions: true
  expression-cache-size: 512
```

## Validation

Built-in validators check:
- **References** - All rule/action references exist
- **Reachability** - All rules are reachable from entry point
- **Cycles** - No infinite loops in rule transitions

```java
CompositeValidator validator = CompositeValidator.createDefault();
ValidationResult result = validator.validate(config);

if (result.hasErrors()) {
    System.err.println(result.getSummary());
}
```

## Testing

```bash
# Unit tests
mvn test

# Integration tests
cd rule-engine-demo
mvn test

# Manual testing
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","amount":150,"items":[...],"verified":true}'
```

## Use Cases

- **Order Processing** - Multi-step approval workflows
- **Pricing Engines** - Dynamic pricing with rules
- **Discount Calculation** - Complex discount logic
- **User Validation** - Multi-criteria user verification
- **Workflow Automation** - Business process automation
- **Decision Trees** - Complex decision-making logic
- **Feature Flags** - Rule-based feature enablement
- **A/B Testing** - Rule-driven test routing

## Performance

- Expression caching for repeated evaluations
- Configurable execution depth limits
- Timeout protection for long-running rules
- Minimal overhead (~1-5ms per rule)

## Best Practices

1. **Keep rules focused** - One responsibility per rule
2. **Use meaningful IDs** - Descriptive rule and action IDs
3. **Validate early** - Enable `validate-on-startup`
4. **Cache expressions** - Enable expression caching
5. **Set timeouts** - Prevent runaway executions
6. **Handle errors** - Use error handlers and `continueOnError`
7. **Log execution** - Track execution history for debugging
8. **Test thoroughly** - Unit test custom actions
9. **Monitor metrics** - Use Spring Boot Actuator
10. **Version rules** - Track rule configuration changes

## Troubleshooting

### RuleEngineService bean not found
Ensure `META-INF/spring.factories` exists with:
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.ruleengine.spring.boot.RuleEngineAutoConfiguration
```

### Rules not loading
- Verify `config-location` path
- Check JSON syntax
- Review validation errors in logs

### Compilation errors
Build modules in order:
```bash
cd rule-execution-engine && mvn clean install
cd ../rule-engine-spring-boot-starter && mvn clean install
cd ../rule-engine-demo && mvn clean install
```

### Expression evaluation fails
- Check JEXL syntax
- Verify variable names match
- Use `util.*` prefix for utility functions

## Contributing

1. Fork the repository
2. Create feature branch
3. Add tests for new features
4. Ensure all tests pass
5. Submit pull request

## Roadmap

- [ ] Database action provider
- [ ] Cache action provider
- [ ] Message queue integration
- [ ] Async action execution
- [ ] Rule versioning
- [ ] A/B testing support
- [ ] Visual rule editor
- [ ] Performance monitoring dashboard
- [ ] Rule hot-reloading
- [ ] Expression debugging tools

## License

[[Apache-2.0 license]](http://www.apache.org/licenses/LICENSE-2.0)

## Support

- Documentation: See individual module READMEs
- Issues: [GitHub Issues](your-repo-url)
- Email: pradyskumar@gmail.com

## Dependencies

- Java 17+
- Spring Boot 3.5.7
- Jackson 2.19.2
- Apache Commons JEXL 3.3
- SLF4J 2.0.17

## Version History

- **0.0.1-SNAPSHOT** - Initial release
  - Core rule engine
  - Spring Boot integration
  - Example demo application
  - API action provider
  - JEXL utility functions
