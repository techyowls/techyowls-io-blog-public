# Java Logging Best Practices

Code samples for: [Java Logging Best Practices: MDC, Structured Logging, and What Not to Log](https://techyowls.io/blog/java-logging-best-practices-slf4j-logback)

## Request Tracing with MDC

Every log line includes the requestId for correlation:

```
14:23:45.123 [http-nio-1] [abc123] INFO  OrderService - Processing order
14:23:45.234 [http-nio-1] [abc123] INFO  PaymentService - Payment processed
14:23:45.345 [http-nio-1] [abc123] INFO  OrderService - Order completed
```

Search logs by requestId: `requestId:abc123`

## Project Structure

```
src/main/
├── java/io/techyowls/logging/
│   ├── filter/
│   │   └── RequestTracingFilter.java    # Adds requestId to MDC
│   ├── service/
│   │   └── OrderService.java            # Logging best practices
│   └── controller/
│       └── OrderController.java
└── resources/
    ├── application.yml
    └── logback-spring.xml               # Profile-based logging config
```

## Run

```bash
# Development (human-readable logs)
./mvnw spring-boot:run

# Production (JSON structured logs)
SPRING_PROFILES_ACTIVE=prod ./mvnw spring-boot:run
```

## Best Practices Demonstrated

### DO

```java
// Use placeholders (lazy evaluation)
log.debug("Processing order {} for customer {}", orderId, customerId);

// Use MDC for request context
MDC.put("orderId", orderId);
log.info("Order processed");

// Log at appropriate levels
log.debug("Starting validation");  // Debug for flow
log.info("Payment of {} processed", amount);  // Info for business events
log.error("Payment failed", exception);  // Error with exception
```

### DON'T

```java
// String concatenation (evaluated even if not logged)
log.debug("Processing " + orderId);  // BAD

// Logging sensitive data
log.info("Credit card: {}", cardNumber);  // BAD

// Excessive logging in loops
for (item : items) {
    log.debug("Processing item");  // BAD - too noisy
}

// Catch-log-rethrow (double logging)
try { ... } catch (Exception e) {
    log.error("Error", e);
    throw e;  // BAD - will be logged again upstream
}
```

## JSON Output (Production)

```json
{
  "timestamp": "2024-01-15T14:23:45.123Z",
  "level": "INFO",
  "logger": "io.techyowls.logging.service.OrderService",
  "message": "Order processed successfully",
  "requestId": "abc123",
  "orderId": "ord-456",
  "customerId": "cust-789",
  "application": "logging-demo",
  "environment": "prod"
}
```

## Log Levels Guide

| Level | Use For |
|-------|---------|
| ERROR | Failures requiring immediate attention |
| WARN | Potentially harmful situations |
| INFO | Business events (order placed, user registered) |
| DEBUG | Diagnostic info for developers |
| TRACE | Very detailed flow (rarely used) |

## License

MIT
