package dev.mathalama.identityservice.infrastructure.messaging;

import dev.mathalama.identityservice.application.dto.event.EventType;
import dev.mathalama.identityservice.infrastructure.persistence.outbox.OutboxEvent;
import dev.mathalama.identityservice.infrastructure.persistence.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay}")
    @Transactional
    public void relayEventsToKafka() {
        List<OutboxEvent> events = outboxRepository.findByProcessedFalseOrderByCreatedAtAsc();

        for (OutboxEvent event : events) {
            try {
                EventType type = EventType.valueOf(event.getEventType());

                String topic = type.getTopic();

                JsonNode payload = objectMapper.readTree(event.getPayload());

                kafkaTemplate.send(topic, event.getAggregateId(),
                        event.getPayload()).get();

                event.setProcessed(true);
                log.debug("Successfully relayed outbox event to Kafka. EventId: {}, Topic: {}", event.getId(), topic);

            } catch ( IllegalArgumentException e) {
                log.error("Unknown event type in DB: {}. Skipping event.", event.getEventType());
                event.setProcessed(true);
            } catch (Exception e) {
                log.error("Failed to relay outbox event to Kafka. EventId: {}. Stopping current batch.", event.getId(), e);
                break;
            }
        }
    }
}