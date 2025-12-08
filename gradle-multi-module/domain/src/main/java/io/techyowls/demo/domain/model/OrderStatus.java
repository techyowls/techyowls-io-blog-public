package io.techyowls.demo.domain.model;

public enum OrderStatus {
    DRAFT,      // Order being created
    SUBMITTED,  // Order placed, awaiting processing
    SHIPPED,    // Order in transit
    DELIVERED,  // Order completed
    CANCELLED   // Order cancelled
}
