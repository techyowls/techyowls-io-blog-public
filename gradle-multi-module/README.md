# Gradle Multi-Module Project

Code samples for: [Gradle Multi-Module Projects with Clean Architecture](https://techyowls.io/blog/gradle-multi-module-project-guide)

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│                         app                                 │
│              (Controllers, Config, DTOs)                    │
│                           │                                 │
│              ┌────────────┼────────────┐                   │
│              ▼                         ▼                   │
│    ┌─────────────────┐      ┌─────────────────┐           │
│    │  infrastructure │      │     domain      │           │
│    │  (JPA Entities, │──────▶   (Pure Java,   │           │
│    │   Repositories) │      │   No Framework) │           │
│    └─────────────────┘      └─────────────────┘           │
└────────────────────────────────────────────────────────────┘

The Dependency Rule:
  outer layers → inner layers (NEVER the reverse)
  app → infrastructure → domain
```

## Module Responsibilities

| Module | Purpose | Dependencies |
|--------|---------|--------------|
| `domain` | Business logic, entities, repository interfaces | None (pure Java) |
| `infrastructure` | JPA entities, repository implementations | domain |
| `app` | REST API, configuration, wiring | domain, infrastructure |

## The Key Insight

The `domain` module has **ZERO framework dependencies**:
- No Spring
- No JPA annotations
- No Hibernate

This means:
- Domain tests are lightning fast (no Spring context)
- Business logic is portable
- Framework upgrades don't touch core business rules

## Run

```bash
./gradlew :app:bootRun
```

## Test

```bash
# All tests
./gradlew test

# Domain tests only (super fast, no Spring)
./gradlew :domain:test

# Integration tests
./gradlew :app:test
```

## API Examples

```bash
# Create order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": "customer-123"}'

# Add item (use order ID from previous response)
curl -X POST http://localhost:8080/api/orders/{orderId}/items \
  -H "Content-Type: application/json" \
  -d '{"productId": "prod-1", "productName": "Widget", "quantity": 2, "unitPrice": 10.00}'

# Submit order
curl -X POST http://localhost:8080/api/orders/{orderId}/submit
```

## Key Files

- `settings.gradle.kts` - Version catalog + module includes
- `domain/build.gradle.kts` - Pure Java, no framework deps
- `infrastructure/src/.../OrderRepositoryImpl.java` - Implements domain port
- `app/src/.../DomainConfig.java` - Wires domain services with Spring

## License

MIT
