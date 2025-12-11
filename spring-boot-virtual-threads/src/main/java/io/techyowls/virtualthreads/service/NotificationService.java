package io.techyowls.virtualthreads.service;

import io.techyowls.virtualthreads.model.Customer;
import io.techyowls.virtualthreads.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Notification service for sending emails/SMS.
 * Uses @Async to not block the main order flow.
 * With virtual threads enabled, @Async methods run on virtual threads.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private static final Random random = new Random();

    @Value("${external.notification.delay-ms:100}")
    private int simulatedDelayMs;

    /**
     * Send order confirmation email asynchronously.
     * This runs on a separate virtual thread so it doesn't block the response.
     */
    @Async
    public CompletableFuture<Void> sendOrderConfirmation(Order order, Customer customer) {
        log.debug("Sending order confirmation on thread: {} (virtual: {})",
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        simulateNetworkDelay();

        log.info("Order confirmation email sent to {} for order {}",
            customer.getEmail(), order.getId());

        return CompletableFuture.completedFuture(null);
    }

    /**
     * Send shipping notification.
     */
    @Async
    public CompletableFuture<Void> sendShippingNotification(Customer customer, Order order, String trackingNumber) {
        log.debug("Sending shipping notification on thread: {} (virtual: {})",
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        simulateNetworkDelay();

        log.info("Shipping notification sent to {} for order {} with tracking {}",
            customer.getEmail(), order.getId(), trackingNumber);

        return CompletableFuture.completedFuture(null);
    }

    private void simulateNetworkDelay() {
        try {
            int delay = simulatedDelayMs + random.nextInt(30);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during notification", e);
        }
    }
}
