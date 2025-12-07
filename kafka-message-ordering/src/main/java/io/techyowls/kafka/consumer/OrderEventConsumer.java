package io.techyowls.kafka.consumer;

import io.techyowls.kafka.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Consumer for key-based ordered events.
 *
 * Events for the same orderId arrive in order because they're in the same partition.
 */
@Service
public class OrderEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    // Track received events per order (for verification)
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> receivedEvents =
        new ConcurrentHashMap<>();

    @KafkaListener(topics = "order-events", groupId = "order-processor")
    public void handleOrderEvent(OrderEvent event) {
        log.info("Received: {} for order {}", event.type(), event.orderId());

        // Track for verification
        receivedEvents
            .computeIfAbsent(event.orderId(), k -> new CopyOnWriteArrayList<>())
            .add(event.type());

        // Process the event
        processEvent(event);
    }

    private void processEvent(OrderEvent event) {
        switch (event.type()) {
            case "CREATED" -> log.info("Order {} created", event.orderId());
            case "PAYMENT_RECEIVED" -> log.info("Payment received for {}", event.orderId());
            case "SHIPPED" -> log.info("Order {} shipped", event.orderId());
            case "DELIVERED" -> log.info("Order {} delivered", event.orderId());
            default -> log.warn("Unknown event type: {}", event.type());
        }
    }

    /**
     * Get received events for an order (for testing).
     */
    public CopyOnWriteArrayList<String> getEventsForOrder(String orderId) {
        return receivedEvents.getOrDefault(orderId, new CopyOnWriteArrayList<>());
    }

    /**
     * Verify events arrived in correct order.
     */
    public boolean verifyOrderingForOrder(String orderId) {
        var events = getEventsForOrder(orderId);
        var expected = java.util.List.of("CREATED", "PAYMENT_RECEIVED", "SHIPPED", "DELIVERED");

        if (events.size() < expected.size()) {
            return false;
        }

        for (int i = 0; i < expected.size(); i++) {
            if (!events.get(i).equals(expected.get(i))) {
                log.error("Order {} out of order! Expected {} at position {}, got {}",
                    orderId, expected.get(i), i, events.get(i));
                return false;
            }
        }
        return true;
    }
}
