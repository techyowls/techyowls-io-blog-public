package io.techyowls.demo.domain.model;

import java.math.BigDecimal;

/**
 * Value object representing an item in an order.
 * Immutable by design - use Java record.
 */
public record OrderItem(
    String productId,
    String productName,
    int quantity,
    BigDecimal unitPrice
) {
    public OrderItem {
        if (productId == null || productId.isBlank()) {
            throw new IllegalArgumentException("Product ID required");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
