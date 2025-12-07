package io.techyowls.streams.model;

import java.math.BigDecimal;

public record OrderItem(
    String productId,
    String productName,
    int quantity,
    BigDecimal price
) {
    public BigDecimal subtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
