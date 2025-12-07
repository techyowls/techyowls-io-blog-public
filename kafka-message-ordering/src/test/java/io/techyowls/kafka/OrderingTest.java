package io.techyowls.kafka;

import io.techyowls.kafka.consumer.OrderEventConsumer;
import io.techyowls.kafka.producer.KeyBasedOrderEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EmbeddedKafka(
    partitions = 3,
    topics = {"order-events"},
    brokerProperties = {"listeners=PLAINTEXT://localhost:9092", "port=9092"}
)
@DirtiesContext
class OrderingTest {

    @Autowired
    private KeyBasedOrderEventProducer producer;

    @Autowired
    private OrderEventConsumer consumer;

    @Test
    void shouldMaintainOrderingForSameKey() {
        // Given
        String orderId = UUID.randomUUID().toString();

        // When - send events for the same order
        producer.processOrder(orderId);

        // Then - events should arrive in order
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            var events = consumer.getEventsForOrder(orderId);
            assertEquals(4, events.size(), "Should receive 4 events");
            assertTrue(consumer.verifyOrderingForOrder(orderId), "Events should be in order");
        });
    }

    @Test
    void shouldMaintainOrderingForMultipleOrders() {
        // Given - 10 concurrent orders
        var orderIds = java.util.stream.IntStream.range(0, 10)
            .mapToObj(i -> UUID.randomUUID().toString())
            .toList();

        // When - send events for all orders
        orderIds.forEach(producer::processOrder);

        // Then - each order's events should be in order
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            for (String orderId : orderIds) {
                var events = consumer.getEventsForOrder(orderId);
                assertEquals(4, events.size(),
                    "Order " + orderId + " should have 4 events");
                assertTrue(consumer.verifyOrderingForOrder(orderId),
                    "Order " + orderId + " events should be in order");
            }
        });
    }
}
