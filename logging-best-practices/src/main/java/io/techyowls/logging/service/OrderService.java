package io.techyowls.logging.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Service demonstrating logging best practices.
 */
@Service
@Slf4j
public class OrderService {

    /**
     * GOOD: Structured logging with context
     */
    public void processOrder(String orderId, String customerId, BigDecimal total) {
        // Add order context to MDC for all downstream logs
        MDC.put("orderId", orderId);
        MDC.put("customerId", customerId);

        try {
            log.info("Processing order for amount: {}", total);

            // Simulate processing
            validateOrder(orderId, total);
            chargeCustomer(customerId, total);
            fulfillOrder(orderId);

            log.info("Order processed successfully");
        } catch (Exception e) {
            // ERROR logs include the exception - stack trace goes to logs
            log.error("Order processing failed", e);
            throw e;
        } finally {
            // Clean up MDC
            MDC.remove("orderId");
            MDC.remove("customerId");
        }
    }

    /**
     * BAD examples (commented out) vs GOOD examples
     */
    private void validateOrder(String orderId, BigDecimal total) {
        // BAD: Don't concatenate strings
        // log.debug("Validating order " + orderId + " with total " + total);

        // GOOD: Use placeholders (lazy evaluation)
        log.debug("Validating order {} with total {}", orderId, total);

        // BAD: Don't log sensitive data
        // log.info("Customer credit card: {}", creditCard);

        // GOOD: Mask sensitive data if needed
        // log.info("Customer card ending in: {}", maskCard(creditCard));
    }

    private void chargeCustomer(String customerId, BigDecimal amount) {
        // BAD: Don't use info for debug-level messages
        // log.info("Starting charge process...");

        // GOOD: Use appropriate log levels
        log.debug("Initiating payment processing");

        // Simulate payment
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // GOOD: Log meaningful events at INFO level
        log.info("Payment processed: {} charged successfully", amount);
    }

    private void fulfillOrder(String orderId) {
        // BAD: Excessive logging in loops
        // for (int i = 0; i < items.size(); i++) {
        //     log.debug("Processing item {}", i);  // Too noisy
        // }

        // GOOD: Log summary or use trace level
        log.debug("Fulfillment started for order");

        // BAD: Catching and logging then rethrowing
        // try { ... } catch (Exception e) {
        //     log.error("Error", e);  // Will be logged twice!
        //     throw e;
        // }

        log.info("Order fulfilled and ready for shipping");
    }
}
