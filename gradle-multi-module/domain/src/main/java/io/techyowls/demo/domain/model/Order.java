package io.techyowls.demo.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Order aggregate root.
 *
 * Notice: NO JPA annotations. This is a pure domain object.
 * The infrastructure layer will map this to a JPA entity.
 */
public class Order {

    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(String customerId) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = new ArrayList<>();
        this.status = OrderStatus.DRAFT;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // Reconstitution constructor (for loading from DB)
    public Order(String id, String customerId, List<OrderItem> items,
                 OrderStatus status, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.items = new ArrayList<>(items);
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ========== Business Methods ==========

    public void addItem(String productId, String productName, int quantity, BigDecimal unitPrice) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify order in status: " + status);
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if product already in order
        for (OrderItem item : items) {
            if (item.productId().equals(productId)) {
                // Update existing item
                items.remove(item);
                items.add(new OrderItem(productId, productName, item.quantity() + quantity, unitPrice));
                this.updatedAt = Instant.now();
                return;
            }
        }

        items.add(new OrderItem(productId, productName, quantity, unitPrice));
        this.updatedAt = Instant.now();
    }

    public void removeItem(String productId) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Cannot modify order in status: " + status);
        }

        boolean removed = items.removeIf(item -> item.productId().equals(productId));
        if (!removed) {
            throw new IllegalArgumentException("Product not in order: " + productId);
        }
        this.updatedAt = Instant.now();
    }

    public void submit() {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Can only submit DRAFT orders");
        }
        if (items.isEmpty()) {
            throw new IllegalStateException("Cannot submit empty order");
        }

        this.status = OrderStatus.SUBMITTED;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED || status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel order in status: " + status);
        }

        this.status = OrderStatus.CANCELLED;
        this.updatedAt = Instant.now();
    }

    public void markShipped() {
        if (status != OrderStatus.SUBMITTED) {
            throw new IllegalStateException("Can only ship SUBMITTED orders");
        }

        this.status = OrderStatus.SHIPPED;
        this.updatedAt = Instant.now();
    }

    public void markDelivered() {
        if (status != OrderStatus.SHIPPED) {
            throw new IllegalStateException("Can only deliver SHIPPED orders");
        }

        this.status = OrderStatus.DELIVERED;
        this.updatedAt = Instant.now();
    }

    // ========== Calculations ==========

    public BigDecimal getTotal() {
        return items.stream()
            .map(OrderItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getItemCount() {
        return items.stream()
            .mapToInt(OrderItem::quantity)
            .sum();
    }

    // ========== Getters (Immutable views) ==========

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
