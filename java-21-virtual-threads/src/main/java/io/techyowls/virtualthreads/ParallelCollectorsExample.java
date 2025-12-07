package io.techyowls.virtualthreads;

import com.pivovarit.collectors.ParallelCollectors;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * Clean, functional approach to virtual threads using parallel-collectors library.
 *
 * Benefits:
 * - Stream-based API
 * - Virtual threads by default in v3.x
 * - Handles back-pressure automatically
 */
public class ParallelCollectorsExample {

    public static void main(String[] args) {
        // Example 1: Simple parallel processing
        List<String> results = IntStream.range(0, 100)
            .boxed()
            .collect(ParallelCollectors.parallel(
                ParallelCollectorsExample::fetchUser,
                toList()
            ))
            .join();

        System.out.println("Fetched " + results.size() + " users");

        // Example 2: With custom parallelism limit
        List<String> limitedResults = IntStream.range(0, 1000)
            .boxed()
            .collect(ParallelCollectors.parallel(
                ParallelCollectorsExample::fetchUser,
                toList(),
                100  // Max 100 concurrent
            ))
            .join();

        System.out.println("Fetched " + limitedResults.size() + " users (limited concurrency)");
    }

    private static String fetchUser(int id) {
        try {
            // Simulate I/O latency
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "user-" + id;
    }
}
