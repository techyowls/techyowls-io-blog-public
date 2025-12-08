package io.techyowls.demo.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Pure unit tests - no Spring, no mocking frameworks needed.
 * The domain module tests FAST because there are no framework deps.
 */
class OrderTest {

    @Test
    void shouldCreateDraftOrder() {
        Order order = new Order("customer-123");

        assertThat(order.getId()).isNotNull();
        assertThat(order.getCustomerId()).isEqualTo("customer-123");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(order.getItems()).isEmpty();
    }

    @Test
    void shouldAddItems() {
        Order order = new Order("customer-123");

        order.addItem("prod-1", "Widget", 2, new BigDecimal("10.00"));
        order.addItem("prod-2", "Gadget", 1, new BigDecimal("25.00"));

        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotal()).isEqualByComparingTo("45.00");
        assertThat(order.getItemCount()).isEqualTo(3);
    }

    @Test
    void shouldCombineDuplicateProducts() {
        Order order = new Order("customer-123");

        order.addItem("prod-1", "Widget", 2, new BigDecimal("10.00"));
        order.addItem("prod-1", "Widget", 3, new BigDecimal("10.00"));

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).quantity()).isEqualTo(5);
        assertThat(order.getTotal()).isEqualByComparingTo("50.00");
    }

    @Test
    void shouldSubmitOrder() {
        Order order = new Order("customer-123");
        order.addItem("prod-1", "Widget", 1, new BigDecimal("10.00"));

        order.submit();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
    }

    @Test
    void shouldNotSubmitEmptyOrder() {
        Order order = new Order("customer-123");

        assertThatThrownBy(order::submit)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("empty");
    }

    @Test
    void shouldNotModifySubmittedOrder() {
        Order order = new Order("customer-123");
        order.addItem("prod-1", "Widget", 1, new BigDecimal("10.00"));
        order.submit();

        assertThatThrownBy(() ->
            order.addItem("prod-2", "Gadget", 1, new BigDecimal("25.00"))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldCancelDraftOrder() {
        Order order = new Order("customer-123");
        order.addItem("prod-1", "Widget", 1, new BigDecimal("10.00"));

        order.cancel();

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void shouldNotCancelShippedOrder() {
        Order order = new Order("customer-123");
        order.addItem("prod-1", "Widget", 1, new BigDecimal("10.00"));
        order.submit();
        order.markShipped();

        assertThatThrownBy(order::cancel)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("SHIPPED");
    }

    @Test
    void shouldFollowOrderLifecycle() {
        Order order = new Order("customer-123");
        order.addItem("prod-1", "Widget", 1, new BigDecimal("10.00"));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.DRAFT);

        order.submit();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUBMITTED);

        order.markShipped();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.SHIPPED);

        order.markDelivered();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.DELIVERED);
    }
}
