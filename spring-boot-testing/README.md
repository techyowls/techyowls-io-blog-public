# Spring Boot Testing Examples

Code samples for: [Spring Boot Testing](https://techyowls.io/blog/spring-boot-testing-complete-guide)

## Run Tests

```bash
./mvnw test
```

## Test Types

| Class | Type | What It Tests |
|-------|------|---------------|
| `OrderServiceTest` | Unit | Service logic with mocks |
| `OrderRepositoryTest` | Integration | JPA with real PostgreSQL |

## Requirements

- Java 21
- Docker (for Testcontainers)

## License

MIT
