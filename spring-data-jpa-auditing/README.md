# Spring Data JPA Auditing

Code samples for: [Spring Data JPA Auditing: Stop Writing createdAt/updatedAt Boilerplate](https://techyowls.io/blog/spring-data-jpa-auditing-complete-guide)

## Project Structure

```
src/main/java/io/techyowls/auditing/
├── config/
│   └── JpaConfig.java           # @EnableJpaAuditing + AuditorAware
├── model/
│   ├── BaseEntity.java          # @CreatedDate, @LastModifiedDate
│   ├── SoftDeleteEntity.java    # Soft delete with audit trail
│   └── Product.java             # @Audited for Envers
├── repository/
│   └── ProductRepository.java
└── service/
    └── ProductAuditService.java # Query revision history
```

## Run

```bash
./mvnw spring-boot:run
```

H2 Console: http://localhost:8080/h2-console

## Key Features

### 1. Automatic Timestamps

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
```

### 2. Track Who Made Changes

```java
@Bean
public AuditorAware<String> auditorAware() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext())
        .map(SecurityContext::getAuthentication)
        .map(Authentication::getName);
}
```

### 3. Full Revision History (Envers)

```java
@Entity
@Audited  // Enable revision tracking
public class Product extends BaseEntity {
    // ...
}
```

Query history:
```java
AuditReader reader = AuditReaderFactory.get(entityManager);
List<Number> revisions = reader.getRevisions(Product.class, productId);
```

## License

MIT
