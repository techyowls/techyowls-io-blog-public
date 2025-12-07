package io.techyowls.testing.repository;

import io.techyowls.testing.model.Order;
import io.techyowls.testing.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests with real PostgreSQL via Testcontainers.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class OrderRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private OrderRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindByUserId() {
        // Given
        entityManager.persist(new Order(null, "user-1", OrderStatus.PENDING));
        entityManager.persist(new Order(null, "user-1", OrderStatus.SHIPPED));
        entityManager.persist(new Order(null, "user-2", OrderStatus.PENDING));
        entityManager.flush();

        // When
        var orders = repository.findByUserId("user-1");

        // Then
        assertThat(orders).hasSize(2);
    }

    @Test
    void shouldFindByStatus() {
        // Given
        entityManager.persist(new Order(null, "user-1", OrderStatus.PENDING));
        entityManager.persist(new Order(null, "user-2", OrderStatus.PENDING));
        entityManager.persist(new Order(null, "user-3", OrderStatus.SHIPPED));
        entityManager.flush();

        // When
        var pendingOrders = repository.findByStatus(OrderStatus.PENDING);

        // Then
        assertThat(pendingOrders).hasSize(2);
    }

    @Test
    void shouldFindPendingOrdersOlderThan() {
        // Given
        Order oldOrder = new Order(null, "user-1", OrderStatus.PENDING);
        oldOrder.setCreatedAt(LocalDateTime.now().minusDays(7));
        entityManager.persist(oldOrder);

        Order newOrder = new Order(null, "user-2", OrderStatus.PENDING);
        newOrder.setCreatedAt(LocalDateTime.now());
        entityManager.persist(newOrder);

        entityManager.flush();

        // When
        var staleOrders = repository.findPendingOrdersOlderThan(
            LocalDateTime.now().minusDays(3)
        );

        // Then
        assertThat(staleOrders).hasSize(1);
    }
}
