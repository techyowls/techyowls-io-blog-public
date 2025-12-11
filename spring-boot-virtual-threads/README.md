# Spring Boot Virtual Threads Demo

A complete example demonstrating **Java 21 Virtual Threads** with **Spring Boot 3.3**.

This project accompanies the tutorial: [Spring Boot 3 Virtual Threads: Complete Guide to Java 21 Concurrency](https://techyowls.io/blog/spring-boot-virtual-threads-java21/)

## Features

- Virtual threads enabled with `spring.threads.virtual.enabled=true`
- E-commerce order processing with multiple I/O operations
- Simulated external services (Inventory, Payment, Notification)
- Custom Prometheus metrics for virtual thread monitoring
- H2 in-memory database for easy testing

## Requirements

- Java 21+
- Maven 3.8+

## Quick Start

```bash
# Clone the repository
git clone https://github.com/Moshiour027/techyowls-io-blog-public.git
cd techyowls-io-blog-public/spring-boot-virtual-threads

# Run the application
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package
java -jar target/virtual-threads-demo-1.0.0.jar
```

## API Endpoints

### Order Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create a new order |
| GET | `/api/orders/{id}` | Get order by ID |
| GET | `/api/orders/customer/{customerId}` | Get orders by customer |
| GET | `/api/orders` | Get all orders |

### Thread Information

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/threads/info` | Get current thread info |
| GET | `/api/threads/slow?delayMs=100` | Simulate slow endpoint |
| GET | `/api/threads/count` | Get thread counts |

### Actuator

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Health check |
| `/actuator/metrics` | Metrics |
| `/actuator/prometheus` | Prometheus metrics |
| `/h2-console` | H2 Database console |

## Example Request

Create an order:

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "items": [
      {"productId": "PROD-001", "quantity": 2},
      {"productId": "PROD-002", "quantity": 1}
    ],
    "paymentInfo": {
      "cardToken": "tok_visa_4242",
      "billingAddress": "123 Main St"
    }
  }'
```

Verify virtual threads are working:

```bash
curl http://localhost:8080/api/threads/info
# Response: {"threadName":"tomcat-handler-0","threadId":52,"isVirtual":true,"state":"RUNNABLE"}
```

## Load Testing

Test with Apache Bench:

```bash
# 1000 requests, 100 concurrent
ab -n 1000 -c 100 http://localhost:8080/api/threads/slow?delayMs=100
```

Or with `hey`:

```bash
hey -n 1000 -c 100 http://localhost:8080/api/threads/slow?delayMs=100
```

## Available Products

| Product ID | Name | Price |
|------------|------|-------|
| PROD-001 | Wireless Headphones | $79.99 |
| PROD-002 | USB-C Hub | $49.99 |
| PROD-003 | Mechanical Keyboard | $129.99 |
| PROD-004 | Monitor Stand | $89.99 |
| PROD-005 | Webcam HD | $69.99 |

## Sample Customers (Pre-loaded)

| ID | Name | Email |
|----|------|-------|
| 1 | John Doe | john@example.com |
| 2 | Jane Smith | jane@example.com |
| 3 | Bob Wilson | bob@example.com |

## Configuration

Key configuration in `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true  # This is the magic line!

external:
  inventory:
    delay-ms: 50    # Simulated API latency
  payment:
    delay-ms: 200   # Payment APIs are slow
  notification:
    delay-ms: 100
```

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                     ORDER SERVICE                                │
│                   (Virtual Threads)                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│   POST /api/orders                                               │
│         │                                                        │
│         ▼                                                        │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│   │   Validate  │───▶│   Check     │───▶│   Reserve   │        │
│   │   Customer  │    │   Inventory │    │   Stock     │        │
│   │   (DB: 20ms)│    │   (50ms)    │    │   (50ms)    │        │
│   └─────────────┘    └─────────────┘    └─────────────┘        │
│                                                │                 │
│                                                ▼                 │
│   ┌─────────────┐    ┌─────────────┐    ┌─────────────┐        │
│   │   Send      │◀───│   Create    │◀───│   Process   │        │
│   │   Email     │    │   Order     │    │   Payment   │        │
│   │   (async)   │    │   (DB)      │    │   (200ms)   │        │
│   └─────────────┘    └─────────────┘    └─────────────┘        │
│                                                                  │
│   Total I/O: ~400ms per request                                 │
│   Virtual threads: Handle 1000s of concurrent requests          │
└─────────────────────────────────────────────────────────────────┘
```

## Tutorial

Read the full tutorial with explanations, benchmarks, and best practices:

**[Spring Boot 3 Virtual Threads: Complete Guide](https://techyowls.io/blog/spring-boot-virtual-threads-java21/)**

## License

MIT License - feel free to use this code in your projects!

## Author

[TechyOwls](https://techyowls.io) - Software engineers who ship.
