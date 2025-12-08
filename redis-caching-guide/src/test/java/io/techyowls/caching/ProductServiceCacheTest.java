package io.techyowls.caching;

import io.techyowls.caching.model.Product;
import io.techyowls.caching.service.ProductRepository;
import io.techyowls.caching.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests with real Redis using Testcontainers.
 */
@SpringBootTest
@Testcontainers
class ProductServiceCacheTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        cacheManager.getCache("products").clear();
    }

    @Test
    void shouldCacheProductById() {
        // Create product
        Product product = productRepository.save(Product.builder()
            .name("Cached Widget")
            .price(new BigDecimal("29.99"))
            .category("Electronics")
            .build());

        // First call - hits DB (slow)
        long start1 = System.currentTimeMillis();
        productService.findById(product.getId());
        long duration1 = System.currentTimeMillis() - start1;

        // Second call - from cache (fast)
        long start2 = System.currentTimeMillis();
        productService.findById(product.getId());
        long duration2 = System.currentTimeMillis() - start2;

        // Cache hit should be significantly faster
        assertThat(duration2).isLessThan(duration1);
        assertThat(duration2).isLessThan(100); // Cache should be < 100ms
    }

    @Test
    void shouldEvictCacheOnDelete() {
        Product product = productRepository.save(Product.builder()
            .name("To Delete")
            .price(BigDecimal.TEN)
            .build());

        // Populate cache
        productService.findById(product.getId());

        // Verify cached
        assertThat(cacheManager.getCache("products").get(product.getId())).isNotNull();

        // Delete
        productService.deleteById(product.getId());

        // Cache should be evicted
        assertThat(cacheManager.getCache("products").get(product.getId())).isNull();
    }

    @Test
    void shouldUpdateCacheOnSave() {
        Product product = productRepository.save(Product.builder()
            .name("Original")
            .price(BigDecimal.TEN)
            .build());

        // Cache the original
        productService.findById(product.getId());

        // Update product
        product.setName("Updated");
        product.setPrice(new BigDecimal("20.00"));
        productService.update(product);

        // Get from cache - should have updated values
        Product cached = productService.findById(product.getId()).orElseThrow();
        assertThat(cached.getName()).isEqualTo("Updated");
        assertThat(cached.getPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldCacheByCategory() {
        productRepository.save(Product.builder().name("A").category("Electronics").price(BigDecimal.TEN).build());
        productRepository.save(Product.builder().name("B").category("Electronics").price(BigDecimal.TEN).build());
        productRepository.save(Product.builder().name("C").category("Books").price(BigDecimal.TEN).build());

        // First call - DB
        long start1 = System.currentTimeMillis();
        productService.findByCategory("Electronics");
        long duration1 = System.currentTimeMillis() - start1;

        // Second call - cache
        long start2 = System.currentTimeMillis();
        productService.findByCategory("Electronics");
        long duration2 = System.currentTimeMillis() - start2;

        assertThat(duration2).isLessThan(duration1);
    }

    @Test
    void shouldClearAllCaches() {
        Product product = productRepository.save(Product.builder()
            .name("Cached")
            .price(BigDecimal.TEN)
            .build());

        // Populate cache
        productService.findById(product.getId());

        // Clear all
        productService.clearAllProducts();

        // Cache should be empty
        assertThat(cacheManager.getCache("products").get(product.getId())).isNull();
    }
}
