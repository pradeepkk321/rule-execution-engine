# Rule Engine Spring Boot Starter

Spring Boot auto-configuration for the Rule Execution Engine.

## Features

- **Auto-configuration** for seamless Spring Boot integration
- **Properties-based configuration** via `application.yml`
- **Automatic bean registration** for `RuleExecutor` and `RuleEngineService`
- **Component scanning** for custom action providers
- **Built-in example actions** (API, Database)

## Installation

```xml
<dependency>
    <groupId>com.pk</groupId>
    <artifactId>rule-engine-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Build locally:
```bash
mvn clean install
```

## Configuration

### application.yml

```yaml
rule-engine:
  # Rule configuration file location
  config-location: classpath:rules/order-processing.json
  
  # Validation on startup
  validate-on-startup: true
  
  # Execution limits
  max-execution-depth: 50
  execution-timeout: 30000
  
  # Expression caching
  cache-expressions: true
  expression-cache-size: 512
```

### Properties

| Property | Default | Description |
|----------|---------|-------------|
| `rule-engine.config-location` | `classpath:rules.json` | Path to rules JSON file |
| `rule-engine.validate-on-startup` | `true` | Validate configuration on startup |
| `rule-engine.max-execution-depth` | `50` | Maximum rule nesting depth |
| `rule-engine.execution-timeout` | `30000` | Timeout in milliseconds |
| `rule-engine.cache-expressions` | `true` | Enable expression caching |
| `rule-engine.expression-cache-size` | `512` | Expression cache size |

## Usage

### Inject RuleEngineService

```java
@Service
public class OrderService {
    
    private final RuleEngineService ruleEngineService;
    
    public OrderService(RuleEngineService ruleEngineService) {
        this.ruleEngineService = ruleEngineService;
    }
    
    public OrderResponse processOrder(OrderRequest request) {
        ExecutionContext context = new ExecutionContext();
        context.setVariable("userId", request.getUserId());
        context.setVariable("amount", request.getAmount());
        
        ExecutionResult result = ruleEngineService.execute(context);
        
        if (result.isSuccess()) {
            return OrderResponse.success(
                context.getVariable("orderId", String.class)
            );
        }
        return OrderResponse.failure(result.getErrorMessage());
    }
}
```

### Multiple Execution Methods

```java
// With ExecutionContext
ExecutionContext context = new ExecutionContext();
context.setVariable("key", "value");
ExecutionResult result = ruleEngineService.execute(context);

// With Map
Map vars = Map.of("key", "value");
ExecutionResult result = ruleEngineService.execute(vars);

// With single variable
ExecutionResult result = ruleEngineService.execute("key", "value");

// Execute and extract result
String orderId = ruleEngineService.executeAndGet(
    context, 
    "orderId", 
    String.class
);
```

## Custom Actions

Create a Spring component implementing `ActionProvider`:

```java
@Component
public class EmailActionProvider implements ActionProvider {
    
    @Autowired
    private EmailService emailService;
    
    @Override
    public boolean supports(String actionType) {
        return "EMAIL".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) {
        return new EmailAction(definition, emailService);
    }
    
    @Override
    public int getPriority() {
        return 200; // Custom actions get high priority
    }
}
```

The starter automatically discovers and registers all `ActionProvider` beans.

## Auto-Configuration Details

The starter provides:

1. **ConfigurationLoader** - Loads rules from configured location
2. **ExpressionEvaluator** - JEXL expression evaluator with utilities
3. **ActionRegistry** - Manages all action providers
4. **RuleExecutor** - Core execution engine
5. **RuleEngineService** - Spring-friendly service wrapper

### Bean Registration Order

1. Built-in actions (priority 0)
2. Framework actions (priority 100)
3. Custom actions (priority 200+)

## Example Action: API Call

```java
@Component
public class ApiActionProvider implements ActionProvider {
    
    private final RestTemplate restTemplate;
    
    public ApiActionProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @Override
    public boolean supports(String actionType) {
        return "API".equalsIgnoreCase(actionType);
    }
    
    @Override
    public Action createAction(ActionDefinition definition) {
        return new ApiAction(definition, restTemplate);
    }
}
```

### Configuration

```json
{
  "actionId": "fetch-user",
  "type": "API",
  "config": {
    "url": "https://api.example.com/users/${userId}",
    "method": "GET",
    "headers": {
      "Authorization": "Bearer ${token}"
    }
  },
  "outputVariable": "userData"
}
```

## Spring Boot Application

```java
@SpringBootApplication
public class Application {
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

## Troubleshooting

### RuleEngineService bean not found

Ensure `META-INF/spring.factories` exists:
```properties
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.ruleengine.spring.boot.RuleEngineAutoConfiguration
```

Or add component scan:
```java
@ComponentScan(basePackages = {"com.demo", "com.ruleengine.spring.boot"})
```

### Configuration file not found

Check `config-location` in `application.yml` and ensure file exists in resources.

## Dependencies

- Spring Boot 3.5.7
- rule-execution-engine 0.0.1-SNAPSHOT
- Spring Web (for RestTemplate)

## License

[Your License]