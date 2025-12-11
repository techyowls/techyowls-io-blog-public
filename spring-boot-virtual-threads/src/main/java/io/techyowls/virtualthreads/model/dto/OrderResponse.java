package io.techyowls.virtualthreads.model.dto;

import io.techyowls.virtualthreads.model.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long orderId,
    Long customerId,
    String status,
    BigDecimal totalAmount,
    String paymentId,
    LocalDateTime createdAt,
    List<OrderItemResponse> items
) {
    public static OrderResponse from(Order order) {
        return new OrderResponse(
            order.getId(),
            order.getCustomerId(),
            order.getStatus(),
            order.getTotalAmount(),
            order.getPaymentId(),
            order.getCreatedAt(),
            order.getItems().stream()
                .map(OrderItemResponse::from)
                .toList()
        );
    }
}
