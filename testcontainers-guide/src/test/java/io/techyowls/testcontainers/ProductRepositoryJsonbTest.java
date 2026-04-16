package io.techyowls.testcontainers;

import io.techyowls.testcontainers.model.Product;
import io.techyowls.testcontainers.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test PostgreSQL-specific JSONB queries.
 * These tests are IMPOSSIBLE with H2 - JSONB doesn't exist there.
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ProductRepositoryJsonbTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void shouldQueryJsonbByColor() {
        // Given - products with JSONB metadata
        productRepository.saveAll(List.of(
            new Product("Red T-Shirt", """
                {"color": "red", "size": "L", "tags": ["sale", "new"]}
                """),
            new Product("Blue Jeans", """
                {"color": "blue", "size": "M", "tags": ["bestseller"]}
                """),
            new Product("Red Sneakers", """
                {"color": "red", "size": "42", "tags": ["sport"]}
                """)
        ));
        entityManager.flush();
        entityManager.clear();

        // When - query JSONB field
        List<Product> redProducts = productRepository.findByColor("red");

        // Then
        assertThat(redProducts)
            .hasSize(2)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("Red T-Shirt", "Red Sneakers");
    }

    @Test
    void shouldQueryJsonbArrayContains() {
        // Given
        productRepository.saveAll(List.of(
            new Product("Sale Item", """
                {"color": "red", "tags": ["sale", "new"]}
                """),
            new Product("Regular Item", """
                {"color": "blue", "tags": ["regular"]}
                """),
            new Product("Another Sale", """
                {"color": "green", "tags": ["sale", "clearance"]}
                """)
        ));
        entityManager.flush();
        entityManager.clear();

        // When - query JSONB array contains
        List<Product> saleItems = productRepository.findByTag("sale");

        // Then
        assertThat(saleItems)
            .hasSize(2)
            .extracting(Product::getName)
            .containsExactlyInAnyOrder("Sale Item", "Another Sale");
    }

    @Test
    void shouldHandleNullMetadata() {
        // Given - product without metadata
        Product product = new Product("No Metadata", null);
        productRepository.save(product);
        entityManager.flush();
        entityManager.clear();

        // When - query should not fail
        List<Product> results = productRepository.findByColor("red");

        // Then - should return empty, not throw
        assertThat(results).isEmpty();
    }
}
