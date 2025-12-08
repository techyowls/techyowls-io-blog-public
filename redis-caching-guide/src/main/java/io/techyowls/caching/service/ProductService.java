package io.techyowls.caching.service;

import io.techyowls.caching.model.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service demonstrating Spring Cache annotations with Redis.
 *
 * Cache-Aside Pattern (Read-Through):
 * 1. Check cache for data
 * 2. If not found, fetch from DB
 * 3. Store in cache, return to caller
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository repository;

    /**
     * @Cacheable - Cache the result
     *
     * First call: hits DB, stores in cache
     * Subsequent calls: returns from cache
     */
    @Cacheable(value = "products", key = "#id")
    public Optional<Product> findById(Long id) {
        log.info("DB QUERY: Finding product by id: {}", id);
        simulateSlowQuery();
        return repository.findById(id);
    }

    /**
     * @Cacheable with custom key
     */
    @Cacheable(value = "products", key = "'category:' + #category")
    public List<Product> findByCategory(String category) {
        log.info("DB QUERY: Finding products by category: {}", category);
        simulateSlowQuery();
        return repository.findByCategory(category);
    }

    /**
     * @CachePut - Always executes method and updates cache
     *
     * Use for updates: ensures cache stays in sync with DB.
     */
    @CachePut(value = "products", key = "#result.id")
    public Product save(Product product) {
        log.info("DB WRITE: Saving product: {}", product.getName());
        return repository.save(product);
    }

    /**
     * @CacheEvict - Remove from cache
     *
     * Use for deletes or when you want to force refresh.
     */
    @CacheEvict(value = "products", key = "#id")
    public void deleteById(Long id) {
        log.info("DB DELETE: Removing product: {}", id);
        repository.deleteById(id);
    }

    /**
     * @Caching - Multiple cache operations
     *
     * When updating a product:
     * 1. Update the product cache entry
     * 2. Evict category caches (they're now stale)
     */
    @Caching(
        put = @CachePut(value = "products", key = "#result.id"),
        evict = @CacheEvict(value = "products", key = "'category:' + #product.category")
    )
    public Product update(Product product) {
        log.info("DB UPDATE: Updating product: {}", product.getId());
        return repository.save(product);
    }

    /**
     * @CacheEvict with allEntries - Nuclear option
     *
     * Clears entire cache. Use sparingly (e.g., bulk imports).
     */
    @CacheEvict(value = "products", allEntries = true)
    public void clearAllProducts() {
        log.info("CACHE CLEAR: Evicting all products from cache");
    }

    /**
     * Conditional caching - only cache if condition is true
     */
    @Cacheable(value = "products", key = "#id", unless = "#result == null")
    public Product findByIdOrNull(Long id) {
        log.info("DB QUERY: Finding product by id (nullable): {}", id);
        return repository.findById(id).orElse(null);
    }

    /**
     * Condition - only cache expensive queries
     */
    @Cacheable(value = "products", key = "'all'", condition = "#result.size() > 10")
    public List<Product> findAll() {
        log.info("DB QUERY: Finding all products");
        simulateSlowQuery();
        return repository.findAll();
    }

    private void simulateSlowQuery() {
        try {
            Thread.sleep(500);  // Simulate slow DB
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
