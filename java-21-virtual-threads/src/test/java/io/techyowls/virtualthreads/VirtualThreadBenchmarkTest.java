package io.techyowls.virtualthreads;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class VirtualThreadBenchmarkTest {

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void virtualThreadsHandle5000ConcurrentTasks() throws Exception {
        long time = VirtualThreadBenchmark.benchmarkVirtualThreads(5_000);

        // Should complete in ~1-2 seconds (IO latency + overhead)
        assertTrue(time < 3000, "Expected < 3000ms, got " + time + "ms");
        System.out.println("5,000 virtual threads completed in " + time + "ms");
    }

    @Test
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    void virtualThreadsHandle20000ConcurrentTasks() throws Exception {
        long time = VirtualThreadBenchmark.benchmarkVirtualThreads(20_000);

        // Should complete in ~1-3 seconds
        assertTrue(time < 5000, "Expected < 5000ms, got " + time + "ms");
        System.out.println("20,000 virtual threads completed in " + time + "ms");
    }

    @Test
    @Timeout(value = 60, unit = TimeUnit.SECONDS)
    void virtualThreadsHandle100000ConcurrentTasks() throws Exception {
        long time = VirtualThreadBenchmark.benchmarkVirtualThreads(100_000);

        // Should complete in ~2-5 seconds
        assertTrue(time < 10_000, "Expected < 10000ms, got " + time + "ms");
        System.out.println("100,000 virtual threads completed in " + time + "ms");
    }
}
