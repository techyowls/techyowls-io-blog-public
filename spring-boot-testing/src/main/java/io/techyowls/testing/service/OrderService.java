package io.techyowls.testing.service;

import io.techyowls.testing.model.Order;
import io.techyowls.testing.model.OrderStatus;
import io.techyowls.testing.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(Order order) {
        order.setStatus(OrderStatus.PENDING);
        return orderRepository.save(order);
    }

    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> findByUserId(String userId) {
        return orderRepository.findByUserId(userId);
    }

    public Order confirmOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .map(order -> {
                if (order.getStatus() != OrderStatus.PENDING) {
                    throw new IllegalStateException("Order must be PENDING to confirm");
                }
                order.setStatus(OrderStatus.CONFIRMED);
                return orderRepository.save(order);
            })
            .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    public void cancelOrder(Long orderId) {
        orderRepository.findById(orderId)
            .ifPresentOrElse(
                order -> {
                    if (order.getStatus() == OrderStatus.SHIPPED) {
                        throw new IllegalStateException("Cannot cancel shipped order");
                    }
                    order.setStatus(OrderStatus.CANCELLED);
                    orderRepository.save(order);
                },
                () -> { throw new OrderNotFoundException(orderId); }
            );
    }
}

class OrderNotFoundException extends RuntimeException {
    OrderNotFoundException(Long id) {
        super("Order not found: " + id);
    }
}
