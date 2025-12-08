package io.techyowls.webflux.service;

import io.techyowls.webflux.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

/**
 * Reactive service demonstrating key WebFlux patterns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;

    /**
     * Simple CRUD - nothing blocks.
     */
    public Mono<Product> createProduct(Product product) {
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());
        return repository.save(product)
            .doOnSuccess(p -> log.info("Created product: {}", p.getId()));
    }

    public Mono<Product> findById(String id) {
        return repository.findById(id);
    }

    public Flux<Product> findAll() {
        return repository.findAll();
    }

    public Mono<Product> updateProduct(String id, Product updates) {
        return repository.findById(id)
            .flatMap(existing -> {
                existing.setName(updates.getName());
                existing.setDescription(updates.getDescription());
                existing.setPrice(updates.getPrice());
                existing.setCategory(updates.getCategory());
                existing.setStockQuantity(updates.getStockQuantity());
                existing.setUpdatedAt(Instant.now());
                return repository.save(existing);
            });
    }

    public Mono<Void> deleteProduct(String id) {
        return repository.deleteById(id);
    }

    /**
     * Streaming example - products stream to client as they arrive.
     * Use with: Accept: text/event-stream
     */
    public Flux<Product> streamProductUpdates() {
        return repository.findAll()
            .delayElements(Duration.ofMillis(500))
            .doOnNext(p -> log.debug("Streaming product: {}", p.getName()));
    }

    /**
     * Error handling with fallback.
     */
    public Mono<Product> findByIdWithFallback(String id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ProductNotFoundException(id)))
            .onErrorResume(e -> {
                log.error("Error finding product: {}", e.getMessage());
                return Mono.just(createPlaceholderProduct());
            });
    }

    /**
     * Combine multiple reactive streams.
     * Real-world example: Fetch product + external inventory check.
     */
    public Mono<ProductWithStock> getProductWithStockStatus(String id) {
        Mono<Product> productMono = repository.findById(id);
        Mono<StockStatus> stockMono = checkExternalInventory(id);

        // Combine both calls (execute in parallel)
        return Mono.zip(productMono, stockMono)
            .map(tuple -> new ProductWithStock(tuple.getT1(), tuple.getT2()));
    }

    /**
     * Batch processing with backpressure.
     * Process products in batches, respecting downstream speed.
     */
    public Flux<Product> processProductsWithBackpressure() {
        return repository.findAll()
            .buffer(10) // Process in batches of 10
            .flatMap(batch -> {
                log.info("Processing batch of {} products", batch.size());
                return Flux.fromIterable(batch)
                    .map(this::enrichProduct);
            }, 2); // Max 2 batches concurrently
    }

    /**
     * Retry pattern for resilience.
     */
    public Flux<Product> findByCategoryWithRetry(String category) {
        return repository.findByCategory(category)
            .retryWhen(reactor.util.retry.Retry.backoff(3, Duration.ofMillis(100))
                .filter(e -> e instanceof RuntimeException)
                .doBeforeRetry(signal -> log.warn("Retrying after error: {}", signal.failure().getMessage())));
    }

    // ========== Helper Methods ==========

    private Mono<StockStatus> checkExternalInventory(String productId) {
        // Simulates external API call
        return Mono.just(new StockStatus(productId, true, 100))
            .delayElement(Duration.ofMillis(50));
    }

    private Product enrichProduct(Product product) {
        // Add computed fields, fetch related data, etc.
        return product;
    }

    private Product createPlaceholderProduct() {
        return Product.builder()
            .id("placeholder")
            .name("Product Not Available")
            .price(BigDecimal.ZERO)
            .build();
    }

    // ========== DTOs ==========

    public record StockStatus(String productId, boolean inStock, int quantity) {}

    public record ProductWithStock(Product product, StockStatus stockStatus) {}

    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String id) {
            super("Product not found: " + id);
        }
    }
}
