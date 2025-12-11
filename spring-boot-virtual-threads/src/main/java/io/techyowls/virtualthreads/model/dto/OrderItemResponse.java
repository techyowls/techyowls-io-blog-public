package io.techyowls.virtualthreads.model.dto;

import io.techyowls.virtualthreads.model.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
    String productId,
    String productName,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
            item.getProductId(),
            item.getProductName(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
        );
    }
}
