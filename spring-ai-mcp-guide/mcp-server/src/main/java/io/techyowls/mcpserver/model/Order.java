package io.techyowls.mcpserver.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Order entity for the demo.
 */
public record Order(
        String orderId,
        String productSku,
        int quantity,
        BigDecimal totalPrice,
        String customerEmail,
        OrderStatus status,
        Instant createdAt
) {
    public static Order create(String productSku, int quantity, BigDecimal unitPrice, String customerEmail) {
        return new Order(
                UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                productSku,
                quantity,
                unitPrice.multiply(BigDecimal.valueOf(quantity)),
                customerEmail,
                OrderStatus.PENDING,
                Instant.now()
        );
    }

    public enum OrderStatus {
        PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    }
}
