package io.techyowls.mcpserver.model;

import java.math.BigDecimal;

/**
 * Product entity for the demo.
 */
public record Product(
        String sku,
        String name,
        String category,
        String description,
        BigDecimal price,
        int stockQuantity
) {
    public boolean isInStock() {
        return stockQuantity > 0;
    }
}
