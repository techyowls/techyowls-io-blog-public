# Redis Caching Patterns with Spring Boot

Code samples for: [Redis Caching Patterns: Cache-Aside, Write-Through, and Thundering Herd Protection](https://techyowls.io/blog/redis-caching-patterns-spring-boot)

## The Problem Caching Solves

```
Without cache:
┌─────────┐    1000 req/sec    ┌────────────┐
│ Clients │ ─────────────────▶ │  Database  │  <- Overloaded!
└─────────┘                    └────────────┘

With cache (95% hit rate):
┌─────────┐    950 req/sec     ┌───────────┐
│ Clients │ ─────────────────▶ │   Redis   │  <- Fast!
└─────────┘         │          └───────────┘
                    │ 50 req/sec
                    ▼
              ┌────────────┐
              │  Database  │  <- Manageable
              └────────────┘
```

## Project Structure

```
src/main/java/io/techyowls/caching/
├── config/
│   └── RedisConfig.java                # JSON serialization, per-cache TTL
├── service/
│   ├── ProductService.java             # @Cacheable, @CacheEvict, @CachePut
│   ├── DistributedLockService.java     # Redis-based distributed locks
│   └── ThunderingHerdProtectedService.java  # Prevents cache stampede
├── controller/
│   └── ProductController.java
└── model/
    └── Product.java
```

## Run

```bash
# Start Redis
docker run -d -p 6379:6379 redis:7-alpine

# Run the app
./mvnw spring-boot:run
```

## Caching Patterns Demonstrated

### 1. Cache-Aside (Read-Through)

```java
@Cacheable(value = "products", key = "#id")
public Optional<Product> findById(Long id) {
    // Only called on cache miss
    return repository.findById(id);
}
```

### 2. Write-Through

```java
@CachePut(value = "products", key = "#result.id")
public Product save(Product product) {
    // Always runs, updates cache with result
    return repository.save(product);
}
```

### 3. Cache Invalidation

```java
@CacheEvict(value = "products", key = "#id")
public void deleteById(Long id) {
    repository.deleteById(id);
}
```

### 4. Thundering Herd Protection

```java
// Only ONE request queries DB on cache miss
// Others wait for cache to be populated
public <T> T getOrCompute(String key, Supplier<T> compute) {
    if (lockService.tryLock("compute:" + key)) {
        T value = compute.get();
        cache.put(key, value);
        return value;
    }
    // Wait and retry from cache
    return waitForCache(key);
}
```

## Cache Configuration

Different TTLs per cache:
```java
Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
cacheConfigs.put("products", defaultConfig.entryTtl(Duration.ofMinutes(30)));
cacheConfigs.put("users", defaultConfig.entryTtl(Duration.ofHours(1)));
cacheConfigs.put("prices", defaultConfig.entryTtl(Duration.ofMinutes(5)));
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products/{id}` | Get (cached) |
| GET | `/api/products` | Get all |
| GET | `/api/products/category/{cat}` | Get by category (cached) |
| POST | `/api/products` | Create (updates cache) |
| PUT | `/api/products/{id}` | Update (updates cache) |
| DELETE | `/api/products/{id}` | Delete (evicts cache) |
| POST | `/api/products/cache/clear` | Clear all caches |

## Test

```bash
./mvnw test
```

## Watch Cache in Action

```bash
# Redis CLI - watch cache operations
redis-cli MONITOR

# In another terminal, make requests
curl http://localhost:8080/api/products/1  # Miss
curl http://localhost:8080/api/products/1  # Hit!
```

## License

MIT
