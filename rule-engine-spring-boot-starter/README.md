# Rule Engine Demo Application

Demo Spring Boot application showcasing the Rule Engine with API actions.

## Features

- **Order processing workflow** with rule-based logic
- **API integration** - Fetches user data from external APIs
- **Tax and discount calculations** using JEXL expressions
- **Multi-step rule execution** with transitions
- **REST API** for order submission
- **Spring Boot Actuator** for monitoring

## Prerequisites

- Java 17+
- Maven 3.6+
- rule-execution-engine installed locally
- rule-engine-spring-boot-starter installed locally

## Installation

```bash
# Clone and build dependencies first
cd rule-execution-engine
mvn clean install

cd ../rule-engine-spring-boot-starter
mvn clean install

# Build demo
cd ../rule-engine-demo
mvn clean install
```

## Running

```bash
mvn spring-boot:run
```

Application starts on `http://localhost:8080`

## API Endpoints

### Process Order

**POST** `/api/orders`

**Request:**
```json
{
  "userId": "1",
  "amount": 150.0,
  "items": [
    {
      "productId": "PROD-1",
      "price": 100.0,
      "quantity": 1
    },
    {
      "productId": "PROD-2",
      "price": 50.0,
      "quantity": 1
    }
  ],
  "verified": true
}
```

**Response:**
```json
{
  "success": true,
  "orderId": "ORD-1-A3F5B2C9",
  "total": 165.0,
  "status": "APPROVED",
  "message": "Thank you Leanne Graham! Your order ORD-1-A3F5B2C9 has been confirmed.",
  "executionTimeMs": 245
}
```

### Health Check

**GET** `/api/orders/health`

```json
{
  "status": "UP",
  "service": "order-processing",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Testing

```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=RuleEngineIntegrationTest

# Test with curl
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "amount": 150.0,
    "items": [
      {"productId": "PROD-1", "price": 100.0, "quantity": 1}
    ],
    "verified": true
  }'
```

## Rule Flow

1. **validate-order** - Fetches user data from API, validates verification
2. **calculate-totals** - Computes subtotal, tax, total, generates order ID
3. **high-value-order** / **standard-order** - Routes based on total amount
4. **verification-required** - Handles unverified users

## Configuration

### application.yml

```yaml
server:
  port: 8080

rule-engine:
  config-location: classpath:rules/order-processing.json
  validate-on-startup: true
  max-execution-depth: 50
  execution-timeout: 30000

logging:
  level:
    com.demo.ruleengine: DEBUG
    com.ruleengine: DEBUG
```

### Rules Configuration

Located at `src/main/resources/rules/order-processing.json`

Key features:
- API action to fetch user data from JSONPlaceholder
- JEXL expressions for calculations
- Conditional transitions based on order value
- Error handling for unverified users

## Project Structure

```
src/
├── main/
│   ├── java/com/demo/ruleengine/
│   │   ├── RuleEngineDemoApplication.java
│   │   ├── action/
│   │   │   └── ApiActionProvider.java
│   │   ├── controller/
│   │   │   └── OrderController.java
│   │   ├── model/
│   │   │   ├── OrderRequest.java
│   │   │   └── OrderResponse.java
│   │   └── service/
│   │       └── OrderProcessingService.java
│   └── resources/
│       ├── application.yml
│       └── rules/
│           └── order-processing.json
└── test/
    └── java/com/demo/ruleengine/
        └── RuleEngineIntegrationTest.java
```

## Custom Actions

### API Action

Makes HTTP calls with variable interpolation:

```json
{
  "actionId": "fetch-user-data",
  "type": "API",
  "config": {
    "url": "https://api.example.com/users/${userId}",
    "method": "GET",
    "headers": {
      "Authorization": "Bearer ${token}"
    }
  },
  "outputVariable": "userData",
  "outputExpression": "result.data"
}
```

Supports:
- GET, POST, PUT, DELETE, PATCH methods
- Variable substitution in URLs and headers
- Request body from context variables
- Response extraction with outputExpression

## Monitoring

Spring Boot Actuator endpoints:

- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Metrics

## Example Scenarios

### Standard Order (amount ≤ $1000)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "amount": 150.0,
    "items": [{"productId": "PROD-1", "price": 150.0, "quantity": 1}],
    "verified": true
  }'
```

Result: `status: "APPROVED"`

### High-Value Order (amount > $1000)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "amount": 1500.0,
    "items": [{"productId": "PROD-1", "price": 1500.0, "quantity": 1}],
    "verified": true
  }'
```

Result: `status: "APPROVED_HIGH_VALUE"` with priority processing message

### Unverified User

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "1",
    "amount": 100.0,
    "items": [{"productId": "PROD-1", "price": 100.0, "quantity": 1}],
    "verified": false
  }'
```

Result: `status: "VERIFICATION_REQUIRED"`

## Extending

### Add New Action Type

1. Create action provider:
```java
@Component
public class DatabaseActionProvider implements ActionProvider {
    @Override
    public boolean supports(String actionType) {
        return "DATABASE".equalsIgnoreCase(actionType);
    }
    // ... implement createAction()
}
```

2. Configure in rules JSON:
```json
{
  "actionId": "save-order",
  "type": "DATABASE",
  "config": {
    "query": "INSERT INTO orders...",
    "params": ["${orderId}", "${total}"]
  }
}
```

### Add New Endpoint

Add to `OrderController.java`:
```java
@GetMapping("/orders/{id}")
public OrderResponse getOrder(@PathVariable String id) {
    // Implementation
}
```

## Troubleshooting

**Port already in use:**
```yaml
server:
  port: 8081  # Change port in application.yml
```

**Rules not loading:**
- Check `config-location` path
- Verify JSON syntax
- Check logs for validation errors

**API action fails:**
- Ensure external API is accessible
- Check network/firewall settings
- Verify URL and credentials

## Dependencies

- Spring Boot 3.5.7
- rule-engine-spring-boot-starter 0.0.1-SNAPSHOT
- Spring Boot Web, Validation, Actuator

## License

[Your License]
```

---

## Quick Setup Guide

### Build Order

```bash
# 1. Core engine
cd rule-execution-engine
mvn clean install

# 2. Spring Boot starter
cd ../rule-engine-spring-boot-starter
mvn clean install

# 3. Demo application
cd ../rule-engine-demo
mvn clean install
mvn spring-boot:run
```

### Test

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":"1","amount":150,"items":[{"productId":"PROD-1","price":150,"quantity":1}],"verified":true}'
```