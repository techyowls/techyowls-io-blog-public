package io.techyowls.demo.infrastructure.persistence;

import io.techyowls.demo.domain.model.OrderItem;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
public class OrderItemJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderJpaEntity order;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    public static OrderItemJpaEntity fromDomain(OrderItem item, OrderJpaEntity order) {
        OrderItemJpaEntity entity = new OrderItemJpaEntity();
        entity.setOrder(order);
        entity.setProductId(item.productId());
        entity.setProductName(item.productName());
        entity.setQuantity(item.quantity());
        entity.setUnitPrice(item.unitPrice());
        return entity;
    }

    public OrderItem toDomain() {
        return new OrderItem(productId, productName, quantity, unitPrice);
    }
}
