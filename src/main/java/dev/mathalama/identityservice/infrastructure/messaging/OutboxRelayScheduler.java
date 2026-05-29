package dev.mathalama.identityservice.infrastructure.messaging;

import dev.mathalama.identityservice.infrastructure.persistence.outbox.OutboxEvent;
import dev.mathalama.identityservice.infrastructure.persistence.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_USER_REGISTERED = "user-registered-topic";

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void relayEventsToKafka() {
        List<OutboxEvent> events = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                String topic = switch (event.getEventType()) {
                    case "USER_REGISTERED" -> TOPIC_USER_REGISTERED;
                    default -> throw new IllegalArgumentException("Unknown event type: " + event.getEventType());
                };

                kafkaTemplate.send(topic, event.getAggregateId(), event.getPayload()).get();

                event.setProcessed(true);
                log.debug("Successfully relayed outbox event to Kafka. EventId: {}", event.getId());

            } catch (Exception e) {
                log.error("Failed to relay outbox event to Kafka. EventId: {}. Stopping current batch.", event.getId(), e);
                break;
            }
        }
    }
}