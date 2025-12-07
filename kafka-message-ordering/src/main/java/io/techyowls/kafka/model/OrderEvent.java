package io.techyowls.kafka.model;

import java.time.Instant;

/**
 * Order event record for demonstrating message ordering.
 */
public record OrderEvent(
    String orderId,
    String type,      // CREATED, PAYMENT_RECEIVED, SHIPPED, DELIVERED
    Instant timestamp,
    long globalSequence  // For external sequencing strategy
) {
    public OrderEvent(String orderId, String type, Instant timestamp) {
        this(orderId, type, timestamp, 0);
    }
}
