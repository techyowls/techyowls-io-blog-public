package io.techyowls.demo.infrastructure.persistence;

import io.techyowls.demo.domain.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository.
 * Internal to infrastructure - not exposed to domain.
 */
interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    List<OrderJpaEntity> findByCustomerId(String customerId);

    List<OrderJpaEntity> findByStatus(OrderStatus status);
}
