package io.techyowls.virtualthreads.config;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Custom metrics for monitoring virtual threads.
 * These metrics are exposed via /actuator/prometheus
 */
@Component
public class VirtualThreadMetrics {

    public VirtualThreadMetrics(MeterRegistry registry) {
        // Track active virtual threads
        Gauge.builder("jvm.threads.virtual.active", this::getVirtualThreadCount)
            .description("Number of active virtual threads")
            .register(registry);

        // Track platform threads
        Gauge.builder("jvm.threads.platform.active", this::getPlatformThreadCount)
            .description("Number of active platform threads")
            .register(registry);
    }

    private long getVirtualThreadCount() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(Thread::isVirtual)
            .count();
    }

    private long getPlatformThreadCount() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(t -> !t.isVirtual())
            .count();
    }
}
