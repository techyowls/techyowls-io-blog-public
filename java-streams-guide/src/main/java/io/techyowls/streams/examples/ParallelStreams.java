package io.techyowls.streams.examples;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.IntStream;

/**
 * Parallel stream examples and best practices
 */
public class ParallelStreams {

    // Basic parallel stream
    public long sumParallel(List<Integer> numbers) {
        return numbers.parallelStream()
            .mapToLong(Integer::longValue)
            .sum();
    }

    // CPU-intensive operation - good for parallel
    public List<Long> computeFactorials(List<Integer> numbers) {
        return numbers.parallelStream()
            .map(this::factorial)
            .toList();
    }

    private long factorial(int n) {
        return IntStream.rangeClosed(1, n)
            .reduce(1, (a, b) -> a * b);
    }

    // Custom thread pool for parallel streams
    public List<String> processWithCustomPool(List<String> items, int parallelism) throws Exception {
        ForkJoinPool customPool = new ForkJoinPool(parallelism);
        try {
            return customPool.submit(() ->
                items.parallelStream()
                    .map(this::expensiveOperation)
                    .toList()
            ).get();
        } finally {
            customPool.shutdown();
        }
    }

    private String expensiveOperation(String input) {
        // Simulate CPU-intensive work
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return input.toUpperCase();
    }

    // Thread-safe accumulation with parallel streams
    public int countMatches(List<String> items, String pattern) {
        LongAdder counter = new LongAdder();
        items.parallelStream()
            .filter(item -> item.contains(pattern))
            .forEach(item -> counter.increment());
        return counter.intValue();
    }

    // Avoid: Stateful operations in parallel streams (BAD)
    public List<Integer> badStatefulOperation(List<Integer> numbers) {
        // DON'T DO THIS - race condition!
        AtomicInteger counter = new AtomicInteger(0);
        return numbers.parallelStream()
            .map(n -> n + counter.incrementAndGet()) // Non-deterministic!
            .toList();
    }

    // Better: Stateless transformation
    public List<Integer> goodStatelessOperation(List<Integer> numbers) {
        return IntStream.range(0, numbers.size())
            .parallel()
            .mapToObj(i -> numbers.get(i) + i + 1)
            .toList();
    }

    // Parallel collection to ConcurrentMap
    public ConcurrentHashMap<Integer, String> parallelToMap(List<String> items) {
        return items.parallelStream()
            .collect(
                ConcurrentHashMap::new,
                (map, item) -> map.put(item.hashCode(), item),
                ConcurrentHashMap::putAll
            );
    }

    // When to use parallel streams:
    // 1. Large datasets (typically > 10,000 elements)
    // 2. CPU-intensive per-element operations
    // 3. Stateless, independent operations
    // 4. No ordering requirements

    // When NOT to use parallel streams:
    // 1. Small datasets (overhead > benefit)
    // 2. I/O-bound operations (use async/reactive instead)
    // 3. Operations that need ordering
    // 4. Shared mutable state
    // 5. Operations with side effects

    // Benchmark helper
    public record BenchmarkResult(String name, long sequentialMs, long parallelMs, double speedup) {}

    public BenchmarkResult benchmark(List<Integer> numbers) {
        // Warm up
        numbers.stream().map(this::factorial).toList();
        numbers.parallelStream().map(this::factorial).toList();

        // Sequential
        long seqStart = System.currentTimeMillis();
        numbers.stream().map(this::factorial).toList();
        long seqTime = System.currentTimeMillis() - seqStart;

        // Parallel
        long parStart = System.currentTimeMillis();
        numbers.parallelStream().map(this::factorial).toList();
        long parTime = System.currentTimeMillis() - parStart;

        return new BenchmarkResult(
            "factorial",
            seqTime,
            parTime,
            parTime > 0 ? (double) seqTime / parTime : 0
        );
    }
}
