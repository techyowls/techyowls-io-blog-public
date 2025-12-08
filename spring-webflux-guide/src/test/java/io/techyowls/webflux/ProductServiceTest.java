package io.techyowls.webflux;

import io.techyowls.webflux.model.Product;
import io.techyowls.webflux.service.ProductRepository;
import io.techyowls.webflux.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

/**
 * Testing reactive streams with StepVerifier.
 *
 * StepVerifier is the key tool for testing reactive code:
 * - Subscribes to the publisher
 * - Verifies emissions step by step
 * - Handles timeouts, errors, completion
 */
@DataMongoTest
@Import(ProductService.class)
class ProductServiceTest {

    @Autowired
    private ProductRepository repository;

    @Autowired
    private ProductService productService;

    @BeforeEach
    void setUp() {
        // Clean DB before each test
        repository.deleteAll().block();
    }

    @Test
    void shouldCreateProduct() {
        Product product = Product.builder()
            .name("Test Widget")
            .price(new BigDecimal("29.99"))
            .category("Electronics")
            .stockQuantity(100)
            .build();

        Mono<Product> result = productService.createProduct(product);

        // StepVerifier verifies the reactive stream
        StepVerifier.create(result)
            .expectNextMatches(saved ->
                saved.getId() != null &&
                saved.getName().equals("Test Widget") &&
                saved.getCreatedAt() != null)
            .verifyComplete();
    }

    @Test
    void shouldFindAllProducts() {
        // Create test data
        Flux.just(
            Product.builder().name("Widget A").price(BigDecimal.TEN).build(),
            Product.builder().name("Widget B").price(BigDecimal.TEN).build(),
            Product.builder().name("Widget C").price(BigDecimal.TEN).build()
        )
        .flatMap(productService::createProduct)
        .blockLast();

        Flux<Product> result = productService.findAll();

        StepVerifier.create(result)
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void shouldHandleEmptyResult() {
        Mono<Product> result = productService.findById("non-existent-id");

        StepVerifier.create(result)
            .verifyComplete(); // Completes without emitting (empty Mono)
    }

    @Test
    void shouldUpdateProduct() {
        // Create initial product
        Product initial = productService.createProduct(
            Product.builder()
                .name("Original")
                .price(new BigDecimal("10.00"))
                .build()
        ).block();

        // Update it
        Product updates = Product.builder()
            .name("Updated")
            .price(new BigDecimal("20.00"))
            .stockQuantity(50)
            .build();

        Mono<Product> result = productService.updateProduct(initial.getId(), updates);

        StepVerifier.create(result)
            .expectNextMatches(updated ->
                updated.getName().equals("Updated") &&
                updated.getPrice().compareTo(new BigDecimal("20.00")) == 0 &&
                updated.getUpdatedAt().isAfter(updated.getCreatedAt()))
            .verifyComplete();
    }

    @Test
    void shouldStreamProducts() {
        // Create test data
        Flux.range(1, 5)
            .map(i -> Product.builder()
                .name("Product " + i)
                .price(BigDecimal.valueOf(i * 10))
                .build())
            .flatMap(productService::createProduct)
            .blockLast();

        Flux<Product> stream = productService.streamProductUpdates();

        // Verify stream emits products with delay
        StepVerifier.create(stream.take(3))
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void shouldCombineProductWithStock() {
        Product product = productService.createProduct(
            Product.builder()
                .name("Stock Test")
                .price(BigDecimal.TEN)
                .build()
        ).block();

        Mono<ProductService.ProductWithStock> result =
            productService.getProductWithStockStatus(product.getId());

        StepVerifier.create(result)
            .expectNextMatches(pws ->
                pws.product().getName().equals("Stock Test") &&
                pws.stockStatus().inStock())
            .verifyComplete();
    }
}
