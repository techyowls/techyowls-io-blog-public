package io.techyowls.demo.domain.service;

import io.techyowls.demo.domain.model.Order;
import io.techyowls.demo.domain.model.OrderStatus;
import io.techyowls.demo.domain.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Domain service for order operations.
 *
 * Contains business logic that doesn't fit naturally in the Order entity.
 * Still framework-agnostic - no Spring annotations here.
 */
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(String customerId) {
        Order order = new Order(customerId);
        return orderRepository.save(order);
    }

    public Order addItemToOrder(String orderId, String productId, String productName,
                                 int quantity, BigDecimal unitPrice) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.addItem(productId, productName, quantity, unitPrice);
        return orderRepository.save(order);
    }

    public Order submitOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.submit();
        return orderRepository.save(order);
    }

    public Order cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();
        return orderRepository.save(order);
    }

    public List<Order> getCustomerOrders(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.SUBMITTED);
    }

    // ========== Exception ==========

    public static class OrderNotFoundException extends RuntimeException {
        public OrderNotFoundException(String orderId) {
            super("Order not found: " + orderId);
        }
    }
}
