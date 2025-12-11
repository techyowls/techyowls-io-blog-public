package io.techyowls.virtualthreads.service;

import io.techyowls.virtualthreads.model.dto.OrderItemRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Simulates an external Inventory Service API.
 * In production, this would make HTTP calls to the actual inventory service.
 * The simulated delays demonstrate how virtual threads handle I/O-bound operations.
 */
@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);
    private static final Random random = new Random();

    @Value("${external.inventory.delay-ms:50}")
    private int simulatedDelayMs;

    // Simulated product catalog
    private static final Map<String, ProductInfo> PRODUCTS = Map.of(
        "PROD-001", new ProductInfo("Wireless Headphones", new BigDecimal("79.99"), 100),
        "PROD-002", new ProductInfo("USB-C Hub", new BigDecimal("49.99"), 50),
        "PROD-003", new ProductInfo("Mechanical Keyboard", new BigDecimal("129.99"), 25),
        "PROD-004", new ProductInfo("Monitor Stand", new BigDecimal("89.99"), 75),
        "PROD-005", new ProductInfo("Webcam HD", new BigDecimal("69.99"), 60)
    );

    public record ProductInfo(String name, BigDecimal price, int stock) {}

    public record InventoryCheckResult(
        boolean allAvailable,
        Map<String, BigDecimal> itemPrices,
        Map<String, String> productNames,
        List<String> unavailableItems
    ) {
        public BigDecimal getPrice(String productId) {
            return itemPrices.get(productId);
        }

        public String getProductName(String productId) {
            return productNames.get(productId);
        }
    }

    /**
     * Check if all requested items are available.
     * This method simulates an I/O-bound operation (API call).
     */
    public InventoryCheckResult checkAvailability(List<OrderItemRequest> items) {
        log.debug("Checking inventory availability on thread: {} (virtual: {})",
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        // Simulate network latency - virtual thread yields during sleep
        simulateNetworkDelay();

        Map<String, BigDecimal> prices = new HashMap<>();
        Map<String, String> names = new HashMap<>();
        List<String> unavailable = new java.util.ArrayList<>();

        for (OrderItemRequest item : items) {
            ProductInfo product = PRODUCTS.get(item.productId());
            if (product == null) {
                unavailable.add(item.productId() + " (not found)");
            } else if (product.stock() < item.quantity()) {
                unavailable.add(item.productId() + " (insufficient stock)");
            } else {
                prices.put(item.productId(), product.price());
                names.put(item.productId(), product.name());
            }
        }

        return new InventoryCheckResult(
            unavailable.isEmpty(),
            prices,
            names,
            unavailable
        );
    }

    /**
     * Reserve stock for an order.
     */
    public void reserveStock(Long orderId, List<OrderItemRequest> items) {
        log.debug("Reserving stock for order {} on thread: {} (virtual: {})",
            orderId,
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        simulateNetworkDelay();
        log.info("Stock reserved for order {}", orderId);
    }

    /**
     * Release reserved stock (compensation).
     */
    public void releaseStock(Long orderId) {
        log.debug("Releasing stock for order {} on thread: {} (virtual: {})",
            orderId,
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        simulateNetworkDelay();
        log.info("Stock released for order {}", orderId);
    }

    private void simulateNetworkDelay() {
        try {
            // Add some variance to simulate real network conditions
            int delay = simulatedDelayMs + random.nextInt(20);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during inventory check", e);
        }
    }
}
