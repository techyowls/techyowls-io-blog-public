# Testcontainers Guide

Code samples for: [Testcontainers: Why Your Integration Tests Have Been Lying to You](https://techyowls.io/blog/testcontainers-spring-boot-integration-testing)

## Project Structure

```
src/
├── main/java/io/techyowls/testcontainers/
│   ├── model/
│   │   ├── User.java
│   │   └── Product.java          # With JSONB field
│   └── repository/
│       ├── UserRepository.java
│       └── ProductRepository.java # PostgreSQL JSONB queries
└── test/java/io/techyowls/testcontainers/
    ├── UserRepositoryTest.java           # Basic @ServiceConnection
    ├── ProductRepositoryJsonbTest.java   # PostgreSQL-specific tests
    └── AbstractIntegrationTest.java      # Shared container pattern
```

## Requirements

- Java 21
- Docker (for Testcontainers)
- Maven 3.8+

## Run Tests

```bash
./mvnw test
```

## Key Patterns

### 1. Basic @ServiceConnection (Spring Boot 3.1+)

```java
@Container
@ServiceConnection  // Auto-configures spring.datasource.*
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
```

### 2. PostgreSQL JSONB Queries

```java
@Query(value = "SELECT * FROM products WHERE metadata->>'color' = :color", nativeQuery = true)
List<Product> findByColor(@Param("color") String color);
```

### 3. Shared Singleton Containers

```java
static {
    POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine").withReuse(true);
    Startables.deepStart(POSTGRES).join();
}
```

## Why Not H2?

| Feature | H2 | PostgreSQL |
|---------|-----|------------|
| JSONB queries | ❌ | ✅ |
| Case sensitivity | Different | Standard |
| Array types | Limited | Full support |
| Window functions | Limited | Full support |

## License

MIT
