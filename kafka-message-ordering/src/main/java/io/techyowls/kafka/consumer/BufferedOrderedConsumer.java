package io.techyowls.kafka.consumer;

import io.techyowls.kafka.model.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

/**
 * Buffered consumer for external sequencing strategy.
 *
 * Collects messages in a time window, sorts by sequence number, then processes.
 * Use this when you need global order across all partitions.
 */
public class BufferedOrderedConsumer {

    private static final Logger log = LoggerFactory.getLogger(BufferedOrderedConsumer.class);

    private final KafkaConsumer<Long, OrderEvent> consumer;
    private final List<OrderEvent> buffer = new ArrayList<>();
    private final Duration bufferWindow;
    private Instant lastProcessTime = Instant.now();

    public BufferedOrderedConsumer(Properties props, Duration bufferWindow) {
        this.consumer = new KafkaConsumer<>(props);
        this.bufferWindow = bufferWindow;
    }

    /**
     * Poll and buffer messages, process when window expires.
     */
    public void poll() {
        ConsumerRecords<Long, OrderEvent> records = consumer.poll(Duration.ofMillis(100));

        for (ConsumerRecord<Long, OrderEvent> record : records) {
            buffer.add(record.value());
        }

        // Process buffer when window expires
        if (Duration.between(lastProcessTime, Instant.now()).compareTo(bufferWindow) > 0) {
            processBuffer();
            lastProcessTime = Instant.now();
        }
    }

    /**
     * Sort buffer by global sequence and process in order.
     */
    private void processBuffer() {
        if (buffer.isEmpty()) {
            return;
        }

        // Sort by global sequence number
        buffer.sort(Comparator.comparingLong(OrderEvent::globalSequence));

        log.info("Processing {} buffered events", buffer.size());

        for (OrderEvent event : buffer) {
            processEvent(event);
        }

        buffer.clear();
    }

    private void processEvent(OrderEvent event) {
        log.info("Processing seq={} type={} orderId={}",
            event.globalSequence(), event.type(), event.orderId());
    }

    public void subscribe(String topic) {
        consumer.subscribe(List.of(topic));
    }

    public void close() {
        processBuffer();  // Process remaining
        consumer.close();
    }
}
