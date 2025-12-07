# Kafka Message Ordering

Code samples for the TechyOwls tutorial: [Kafka Message Ordering: Guarantee Sequence Across Partitions](https://techyowls.io/blog/kafka-message-ordering-complete-guide)

## Prerequisites

- Java 21+
- Maven 3.8+
- Docker (for Testcontainers)

## Strategies Demonstrated

| Strategy | Class | Use Case |
|----------|-------|----------|
| Key-based routing | `KeyBasedOrderEventProducer` | Order per entity (user, order) |
| Idempotent producer | `KafkaConfig` | Prevent duplicates on retry |
| External sequencing | `SequencedProducer` | Global order across partitions |
| Buffered consumer | `BufferedOrderedConsumer` | Reorder by sequence number |

## Run Tests

```bash
./mvnw test
```

Tests use embedded Kafka, no external setup needed.

## Run with Docker

```bash
# Start Kafka
docker-compose up -d

# Run the app
./mvnw spring-boot:run
```

## Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3'
services:
  kafka:
    image: confluentinc/cp-kafka:7.5.0
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_NODE_ID: 1
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@localhost:9093
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092,CONTROLLER://0.0.0.0:9093
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      CLUSTER_ID: kafka-ordering-demo
```

## Key Classes

| Class | Purpose |
|-------|---------|
| `OrderEvent` | Event model with sequence support |
| `KafkaConfig` | Idempotent producer configuration |
| `KeyBasedOrderEventProducer` | Send events with key-based routing |
| `SequencedProducer` | Global sequencing for cross-partition order |
| `OrderEventConsumer` | Standard consumer with ordering verification |
| `BufferedOrderedConsumer` | Buffer and reorder by sequence |

## Configuration Highlights

### Idempotent Producer (prevents duplicates/reordering on retry)

```java
config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
config.put(ProducerConfig.ACKS_CONFIG, "all");
config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
```

### Key-Based Routing

```java
// All events for orderId go to same partition
kafkaTemplate.send(TOPIC, event.orderId(), event);
```

## Decision Framework

```
Need ordering?
├── No → Multi-partition (max throughput)
└── Yes → Per-entity or global?
    ├── Per-entity → Key-based routing + idempotent producer
    └── Global → External sequencing + buffered consumer
```

## License

MIT
