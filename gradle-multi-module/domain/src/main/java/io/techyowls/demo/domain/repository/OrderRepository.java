package io.techyowls.demo.domain.repository;

import io.techyowls.demo.domain.model.Order;
import io.techyowls.demo.domain.model.OrderStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface (Port) for Order persistence.
 *
 * This is a PORT in hexagonal architecture terms.
 * The infrastructure module provides the ADAPTER (implementation).
 *
 * Notice: No JPA, no Spring annotations. Pure Java interface.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(String id);

    List<Order> findByCustomerId(String customerId);

    List<Order> findByStatus(OrderStatus status);

    void delete(Order order);
}
