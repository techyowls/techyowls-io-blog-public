package io.techyowls.testing.service;

import io.techyowls.testing.model.Order;
import io.techyowls.testing.model.OrderStatus;
import io.techyowls.testing.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService - fast, isolated, mocked dependencies.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void shouldCreateOrderWithPendingStatus() {
        // Given
        Order order = new Order(null, "user-123", null);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(1L);
            return o;
        });

        // When
        Order created = orderService.createOrder(order);

        // Then
        assertThat(created.getId()).isEqualTo(1L);
        assertThat(created.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldConfirmPendingOrder() {
        // Given
        Order pendingOrder = new Order(1L, "user-123", OrderStatus.PENDING);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenReturn(pendingOrder);

        // When
        Order confirmed = orderService.confirmOrder(1L);

        // Then
        assertThat(confirmed.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    void shouldThrowWhenConfirmingNonPendingOrder() {
        // Given
        Order shippedOrder = new Order(1L, "user-123", OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

        // When/Then
        assertThatThrownBy(() -> orderService.confirmOrder(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("PENDING");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldNotCancelShippedOrder() {
        // Given
        Order shippedOrder = new Order(1L, "user-123", OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(shippedOrder));

        // When/Then
        assertThatThrownBy(() -> orderService.cancelOrder(1L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("shipped");
    }
}
