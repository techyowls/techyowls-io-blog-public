package io.techyowls.webflux;

import io.techyowls.webflux.model.Product;
import io.techyowls.webflux.service.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;

/**
 * WebTestClient for testing WebFlux endpoints.
 *
 * WebTestClient is the reactive equivalent of MockMvc.
 * It supports both blocking and non-blocking assertion styles.
 */
@SpringBootTest
@AutoConfigureWebTestClient
class ProductControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll().block();
    }

    @Test
    void shouldCreateProduct() {
        webTestClient.post()
            .uri("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "Test Widget",
                    "description": "A test product",
                    "price": 29.99,
                    "category": "Electronics",
                    "stockQuantity": 100
                }
                """)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.name").isEqualTo("Test Widget")
            .jsonPath("$.price").isEqualTo(29.99);
    }

    @Test
    void shouldGetProduct() {
        // Create product first
        Product product = repository.save(
            Product.builder()
                .name("Fetch Test")
                .price(new BigDecimal("19.99"))
                .build()
        ).block();

        webTestClient.get()
            .uri("/api/products/{id}", product.getId())
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Fetch Test");
    }

    @Test
    void shouldGetAllProducts() {
        // Create test products
        repository.save(Product.builder().name("A").price(BigDecimal.TEN).build()).block();
        repository.save(Product.builder().name("B").price(BigDecimal.TEN).build()).block();

        webTestClient.get()
            .uri("/api/products")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Product.class)
            .hasSize(2);
    }

    @Test
    void shouldStreamProducts() {
        // Create test products
        repository.save(Product.builder().name("Stream1").price(BigDecimal.TEN).build()).block();
        repository.save(Product.builder().name("Stream2").price(BigDecimal.TEN).build()).block();

        webTestClient.get()
            .uri("/api/products/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM)
            .returnResult(Product.class)
            .getResponseBody()
            .take(2)
            .collectList()
            .block();
        // Stream received successfully
    }

    @Test
    void shouldValidateProductInput() {
        webTestClient.post()
            .uri("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {
                    "name": "",
                    "price": -1
                }
                """)
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void shouldDeleteProduct() {
        Product product = repository.save(
            Product.builder().name("ToDelete").price(BigDecimal.TEN).build()
        ).block();

        webTestClient.delete()
            .uri("/api/products/{id}", product.getId())
            .exchange()
            .expectStatus().isNoContent();

        // Verify deletion
        webTestClient.get()
            .uri("/api/products/{id}", product.getId())
            .exchange()
            .expectBody().isEmpty();
    }

    @Test
    void shouldUseFunctionalEndpoints() {
        // Test the functional router endpoints
        webTestClient.post()
            .uri("/fn/products")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(Product.builder()
                .name("Functional Test")
                .price(new BigDecimal("15.00"))
                .build())
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Functional Test");
    }
}
