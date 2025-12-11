package io.techyowls.virtualthreads.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Random;
import java.util.UUID;

/**
 * Simulates an external Payment Gateway API.
 * In production, this would integrate with Stripe, PayPal, etc.
 * Payment processing is typically the slowest I/O operation.
 */
@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private static final Random random = new Random();

    @Value("${external.payment.delay-ms:200}")
    private int simulatedDelayMs;

    public record PaymentRequest(
        String cardToken,
        BigDecimal amount,
        String orderId
    ) {}

    public record PaymentResult(
        boolean success,
        String transactionId,
        String errorMessage
    ) {}

    /**
     * Process a payment.
     * This method simulates the slowest external API call (payment processing).
     * Virtual threads shine here - 200ms delay doesn't block the carrier thread.
     */
    public PaymentResult processPayment(PaymentRequest request) {
        log.debug("Processing payment for order {} on thread: {} (virtual: {})",
            request.orderId(),
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        // Simulate payment gateway latency
        simulateNetworkDelay();

        // Simulate 95% success rate
        if (random.nextDouble() > 0.05) {
            String transactionId = "txn_" + UUID.randomUUID().toString().substring(0, 8);
            log.info("Payment successful for order {}: {}", request.orderId(), transactionId);
            return new PaymentResult(true, transactionId, null);
        } else {
            log.warn("Payment failed for order {}", request.orderId());
            return new PaymentResult(false, null, "Payment declined by issuer");
        }
    }

    /**
     * Refund a payment.
     */
    public void refundPayment(String paymentId) {
        log.debug("Refunding payment {} on thread: {} (virtual: {})",
            paymentId,
            Thread.currentThread().getName(),
            Thread.currentThread().isVirtual());

        simulateNetworkDelay();
        log.info("Payment {} refunded", paymentId);
    }

    private void simulateNetworkDelay() {
        try {
            int delay = simulatedDelayMs + random.nextInt(50);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted during payment processing", e);
        }
    }
}
