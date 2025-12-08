package io.techyowls.demo.api;

import io.techyowls.demo.domain.model.Order;
import io.techyowls.demo.domain.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Order order = orderService.createOrder(request.customerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/items")
    public ResponseEntity<OrderResponse> addItem(
            @PathVariable String orderId,
            @Valid @RequestBody AddItemRequest request) {

        Order order = orderService.addItemToOrder(
            orderId,
            request.productId(),
            request.productName(),
            request.quantity(),
            request.unitPrice()
        );
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/submit")
    public ResponseEntity<OrderResponse> submitOrder(@PathVariable String orderId) {
        Order order = orderService.submitOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable String orderId) {
        Order order = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(@PathVariable String customerId) {
        List<OrderResponse> orders = orderService.getCustomerOrders(customerId).stream()
            .map(OrderResponse::from)
            .toList();
        return ResponseEntity.ok(orders);
    }

    // ========== DTOs ==========

    public record CreateOrderRequest(
        @NotBlank String customerId
    ) {}

    public record AddItemRequest(
        @NotBlank String productId,
        @NotBlank String productName,
        @NotNull @Positive Integer quantity,
        @NotNull @Positive BigDecimal unitPrice
    ) {}

    public record OrderResponse(
        String id,
        String customerId,
        String status,
        BigDecimal total,
        int itemCount,
        List<OrderItemResponse> items
    ) {
        public static OrderResponse from(Order order) {
            return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus().name(),
                order.getTotal(),
                order.getItemCount(),
                order.getItems().stream()
                    .map(item -> new OrderItemResponse(
                        item.productId(),
                        item.productName(),
                        item.quantity(),
                        item.unitPrice(),
                        item.getSubtotal()
                    ))
                    .toList()
            );
        }
    }

    public record OrderItemResponse(
        String productId,
        String productName,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
    ) {}
}
