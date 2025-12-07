package io.techyowls.kafka.producer;

import io.techyowls.kafka.model.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Key-based routing - the most common ordering strategy.
 *
 * All events for the same orderId go to the same partition,
 * guaranteeing order per entity.
 */
@Service
public class KeyBasedOrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(KeyBasedOrderEventProducer.class);
    private static final String TOPIC = "order-events";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public KeyBasedOrderEventProducer(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Send order event with orderId as key.
     * This ensures all events for the same order go to the same partition.
     */
    public void sendOrderEvent(String orderId, String eventType) {
        OrderEvent event = new OrderEvent(orderId, eventType, Instant.now());

        // Key = orderId -> same partition -> guaranteed order
        kafkaTemplate.send(TOPIC, event.orderId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to send {}: {}", eventType, ex.getMessage());
                } else {
                    log.info("Sent {} for order {} to partition {}",
                        eventType, orderId, result.getRecordMetadata().partition());
                }
            });
    }

    /**
     * Simulate a complete order lifecycle.
     */
    public void processOrder(String orderId) {
        // These will arrive in order because same key -> same partition
        sendOrderEvent(orderId, "CREATED");
        sendOrderEvent(orderId, "PAYMENT_RECEIVED");
        sendOrderEvent(orderId, "SHIPPED");
        sendOrderEvent(orderId, "DELIVERED");
    }
}
