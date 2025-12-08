# Spring WebFlux Complete Guide

Code samples for: [Spring WebFlux: When to Use (and When NOT To)](https://techyowls.io/blog/spring-webflux-reactive-programming-guide)

## When to Use WebFlux

```
Good fit:
┌─────────────────────────────────────────────────────────────┐
│  ✅ High concurrency with mostly I/O-bound operations       │
│  ✅ Streaming data (real-time feeds, SSE)                   │
│  ✅ Microservices making many external API calls            │
│  ✅ Already using reactive data stores (MongoDB, R2DBC)     │
└─────────────────────────────────────────────────────────────┘

Not a good fit:
┌─────────────────────────────────────────────────────────────┐
│  ❌ CPU-intensive operations                                 │
│  ❌ JDBC/JPA with traditional RDBMS                         │
│  ❌ Team unfamiliar with reactive programming               │
│  ❌ Simple CRUD with low concurrency                        │
└─────────────────────────────────────────────────────────────┘
```

## Project Structure

```
src/main/java/io/techyowls/webflux/
├── controller/
│   └── ProductController.java     # @RestController approach
├── config/
│   ├── RouterConfig.java          # Functional endpoints
│   └── GlobalExceptionHandler.java
├── service/
│   ├── ProductService.java        # Reactive patterns
│   └── ProductRepository.java
├── client/
│   └── ExternalApiClient.java     # WebClient examples
└── model/
    └── Product.java
```

## Run

```bash
# Start MongoDB first
docker run -d -p 27017:27017 mongo:7

# Run the app
./mvnw spring-boot:run
```

## Key Concepts Demonstrated

### 1. Mono and Flux

```java
Mono<Product> single = service.findById(id);      // 0 or 1 item
Flux<Product> stream = service.findAll();          // 0 to N items
```

### 2. Server-Sent Events (SSE)

```java
@GetMapping(value = "/stream", produces = TEXT_EVENT_STREAM_VALUE)
public Flux<Product> stream() {
    return service.streamProductUpdates();
}
```

### 3. WebClient (Reactive HTTP Client)

```java
webClient.get()
    .uri("/posts/{id}", id)
    .retrieve()
    .bodyToMono(Post.class)
    .retryWhen(Retry.backoff(3, Duration.ofMillis(100)));
```

### 4. Combining Streams

```java
Mono.zip(productMono, stockMono)
    .map(tuple -> new ProductWithStock(tuple.getT1(), tuple.getT2()));
```

### 5. Testing with StepVerifier

```java
StepVerifier.create(productService.findById(id))
    .expectNextMatches(p -> p.getName().equals("Widget"))
    .verifyComplete();
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/products` | Create product |
| GET | `/api/products/{id}` | Get by ID |
| GET | `/api/products` | Get all |
| GET | `/api/products/stream` | SSE stream |
| PUT | `/api/products/{id}` | Update |
| DELETE | `/api/products/{id}` | Delete |

Functional endpoints at `/fn/products/*`

## Test

```bash
# All tests
./mvnw test

# Specific test
./mvnw test -Dtest=ProductServiceTest
```

## WebFlux vs Virtual Threads (Java 21)

For new projects on Java 21+, consider Virtual Threads first:
- Familiar blocking code style
- Works with existing libraries (JDBC, JPA)
- Simpler debugging and stack traces

Use WebFlux when:
- You need true streaming (SSE, WebSocket)
- Already using reactive data stores
- Team has reactive experience

## License

MIT
