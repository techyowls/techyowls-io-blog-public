package io.techyowls.streams.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record Order(
    String id,
    String customerId,
    LocalDate orderDate,
    OrderStatus status,
    List<OrderItem> items
) {
    public BigDecimal total() {
        return items.stream()
            .map(OrderItem::subtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
