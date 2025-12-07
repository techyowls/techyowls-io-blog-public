package io.techyowls.kafka.producer;

import io.techyowls.kafka.model.OrderEvent;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

/**
 * External sequencing strategy for global ordering across all partitions.
 *
 * Adds a global sequence number to each message.
 * Consumer must buffer and reorder by sequence.
 */
public class SequencedProducer {

    private static final Logger log = LoggerFactory.getLogger(SequencedProducer.class);
    private static final String TOPIC = "sequenced-events";

    private final AtomicLong sequenceGenerator = new AtomicLong(0);
    private final KafkaProducer<Long, OrderEvent> producer;

    public SequencedProducer(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Idempotent producer
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        props.put(ProducerConfig.ACKS_CONFIG, "all");

        this.producer = new KafkaProducer<>(props);
    }

    /**
     * Send event with global sequence number.
     */
    public void send(String orderId, String eventType) {
        long seq = sequenceGenerator.incrementAndGet();

        OrderEvent event = new OrderEvent(
            orderId,
            eventType,
            Instant.now(),
            seq  // Global sequence number
        );

        // Use sequence as key for consistent routing
        ProducerRecord<Long, OrderEvent> record = new ProducerRecord<>(TOPIC, seq, event);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send seq={}: {}", seq, exception.getMessage());
            } else {
                log.info("Sent seq={} type={} to partition={}",
                    seq, eventType, metadata.partition());
            }
        });
    }

    public void close() {
        producer.close();
    }
}
