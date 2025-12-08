package io.techyowls.demo.infrastructure.persistence;

import io.techyowls.demo.domain.model.Order;
import io.techyowls.demo.domain.model.OrderStatus;
import io.techyowls.demo.domain.repository.OrderRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository adapter - implements domain port using Spring Data JPA.
 *
 * This is the ADAPTER in hexagonal architecture.
 * It bridges the domain interface (port) with the infrastructure (JPA).
 */
@Repository
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository jpaRepository;

    public OrderRepositoryImpl(OrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = OrderJpaEntity.fromDomain(order);
        OrderJpaEntity saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Order> findById(String id) {
        return jpaRepository.findById(id)
            .map(OrderJpaEntity::toDomain);
    }

    @Override
    public List<Order> findByCustomerId(String customerId) {
        return jpaRepository.findByCustomerId(customerId).stream()
            .map(OrderJpaEntity::toDomain)
            .toList();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        return jpaRepository.findByStatus(status).stream()
            .map(OrderJpaEntity::toDomain)
            .toList();
    }

    @Override
    public void delete(Order order) {
        jpaRepository.deleteById(order.getId());
    }
}
