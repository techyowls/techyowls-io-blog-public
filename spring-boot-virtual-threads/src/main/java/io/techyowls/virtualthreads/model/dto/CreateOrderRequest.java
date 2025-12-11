package io.techyowls.virtualthreads.model.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOrderRequest(
    @NotNull(message = "Customer ID is required")
    Long customerId,

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    List<OrderItemRequest> items,

    @NotNull(message = "Payment info is required")
    @Valid
    PaymentInfo paymentInfo
) {}
