# Java 21 Virtual Threads Examples

Code samples for the TechyOwls tutorial: [Java 21 Virtual Threads: The Complete Guide](https://techyowls.io/blog/java-21-virtual-threads-complete-guide)

## Prerequisites

- Java 21+
- Maven 3.8+

## Run Benchmarks

```bash
./mvnw compile exec:java -Dexec.mainClass="io.techyowls.virtualthreads.VirtualThreadBenchmark"
```

## Run Tests

```bash
./mvnw test
```

## Key Classes

| Class | Purpose |
|-------|---------|
| `VirtualThreadBenchmark` | Compare OS threads vs virtual threads |
| `ParallelCollectorsExample` | Functional-style parallel processing |
| `PinningExample` | Demonstrate carrier pinning issues |
| `SpringBootVirtualThreadConfig` | Spring Boot configuration |

## Expected Results

```
=== Testing 5,000 concurrent operations ===
Virtual Threads: 1,101 ms
OS Threads: 1,321 ms

=== Testing 20,000 concurrent operations ===
Virtual Threads: 1,219 ms
OS Threads: SKIPPED (would crash)

=== Testing 100,000 concurrent operations ===
Virtual Threads: 1,587 ms
OS Threads: SKIPPED (would crash)
```

## Detect Pinning

```bash
java -Djdk.tracePinnedThreads=full -cp target/classes io.techyowls.virtualthreads.PinningExample
```

## License

MIT
