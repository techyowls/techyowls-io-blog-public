package io.techyowls.logging.controller;

import io.techyowls.logging.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        String orderId = UUID.randomUUID().toString().substring(0, 8);

        orderService.processOrder(orderId, request.customerId(), request.total());

        return new OrderResponse(orderId, "COMPLETED");
    }

    public record CreateOrderRequest(String customerId, BigDecimal total) {}
    public record OrderResponse(String orderId, String status) {}
}
