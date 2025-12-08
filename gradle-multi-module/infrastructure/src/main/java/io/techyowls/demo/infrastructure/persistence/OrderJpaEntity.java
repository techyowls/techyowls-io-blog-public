package io.techyowls.demo.infrastructure.persistence;

import io.techyowls.demo.domain.model.Order;
import io.techyowls.demo.domain.model.OrderItem;
import io.techyowls.demo.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity - Infrastructure concern.
 *
 * This class knows about JPA annotations, database columns, etc.
 * It maps to/from the domain Order object.
 *
 * Why separate from domain Order?
 * - Domain object is framework-agnostic, testable, pure Java
 * - JPA entity has infrastructure concerns (lazy loading, proxies, etc.)
 * - They can evolve independently
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class OrderJpaEntity {

    @Id
    private String id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemJpaEntity> items = new ArrayList<>();

    // ========== Mapping Methods ==========

    /**
     * Convert domain Order to JPA entity.
     */
    public static OrderJpaEntity fromDomain(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity();
        entity.setId(order.getId());
        entity.setCustomerId(order.getCustomerId());
        entity.setStatus(order.getStatus());
        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        // Map items
        List<OrderItemJpaEntity> itemEntities = order.getItems().stream()
            .map(item -> OrderItemJpaEntity.fromDomain(item, entity))
            .toList();
        entity.setItems(new ArrayList<>(itemEntities));

        return entity;
    }

    /**
     * Convert JPA entity to domain Order.
     */
    public Order toDomain() {
        List<OrderItem> domainItems = items.stream()
            .map(OrderItemJpaEntity::toDomain)
            .toList();

        return new Order(
            id,
            customerId,
            domainItems,
            status,
            createdAt,
            updatedAt
        );
    }
}
