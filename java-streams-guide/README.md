# Java Streams API Guide

Code samples for: [Java Streams API Complete Guide](https://techyowls.io/blog/java-streams-api-complete-guide)

## Project Structure

```
src/main/java/io/techyowls/streams/
├── model/
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   └── Customer.java
└── examples/
    ├── BasicOperations.java      # filter, map, flatMap, reduce
    ├── CollectorExamples.java    # groupingBy, toMap, teeing
    ├── RealWorldPatterns.java    # DTO transform, analytics
    └── ParallelStreams.java      # Parallel processing
```

## Build & Test

```bash
./mvnw clean test
```

## Key Examples

### Basic Operations

```java
// Filter + Map + Reduce
int sumOfSquaresOfEvens = numbers.stream()
    .filter(n -> n % 2 == 0)
    .map(n -> n * n)
    .reduce(0, Integer::sum);

// FlatMap nested structures
List<String> words = sentences.stream()
    .flatMap(s -> Stream.of(s.split("\\s+")))
    .toList();
```

### Collectors

```java
// Group and count
Map<OrderStatus, Long> countByStatus = orders.stream()
    .collect(groupingBy(Order::status, counting()));

// Multi-level grouping
Map<OrderStatus, Map<String, List<Order>>> nested = orders.stream()
    .collect(groupingBy(
        Order::status,
        groupingBy(Order::customerId)
    ));

// Teeing (Java 12+)
record Stats(long count, BigDecimal total) {}
Stats stats = orders.stream()
    .collect(teeing(
        counting(),
        reducing(BigDecimal.ZERO, Order::total, BigDecimal::add),
        Stats::new
    ));
```

### Real-World Patterns

```java
// DTO transformation
List<OrderSummary> summaries = orders.stream()
    .map(o -> new OrderSummary(o.id(), o.status().name(), o.total()))
    .toList();

// Optional filter criteria
List<Order> results = orders.stream()
    .filter(o -> customerId.map(c -> o.customerId().equals(c)).orElse(true))
    .filter(o -> status.map(s -> o.status() == s).orElse(true))
    .toList();

// Top N by criteria
List<Order> topOrders = orders.stream()
    .sorted(comparing(Order::total).reversed())
    .limit(10)
    .toList();
```

### Parallel Streams

```java
// Custom thread pool
ForkJoinPool pool = new ForkJoinPool(4);
List<String> result = pool.submit(() ->
    items.parallelStream()
        .map(this::expensiveOperation)
        .toList()
).get();
```

## When to Use Parallel Streams

| Use Case | Recommendation |
|----------|----------------|
| Large dataset (10k+ items) | ✅ Consider parallel |
| CPU-intensive operations | ✅ Good candidate |
| I/O operations | ❌ Use async instead |
| Shared mutable state | ❌ Avoid parallel |
| Small datasets | ❌ Sequential is faster |

## License

MIT
