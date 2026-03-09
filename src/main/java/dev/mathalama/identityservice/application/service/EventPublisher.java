package dev.mathalama.identityservice.application.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.infrastructure.config.RabbitMQConfig;
import dev.mathalama.sharedevents.domain.event.UserRegisteredEvent;

/**
 * Service for publishing domain events to RabbitMQ
 */
@Service
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public EventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publish UserRegisteredEvent when a new user is created
     */
    public void publishUserRegistered(Users user, String authProvider) {
        try {
            UserRegisteredEvent event = new UserRegisteredEvent(
                    user.getId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getUsername(), // Using username as fullName for now
                    authProvider
            );

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.USER_EVENTS_EXCHANGE,
                    RabbitMQConfig.USER_REGISTERED_ROUTING_KEY,
                    event
            );

            logger.info("✓ UserRegisteredEvent published: {} ({})", user.getEmail(), authProvider);
        } catch (Exception e) {
            logger.error("✗ Failed to publish UserRegisteredEvent for user {}: {}", user.getEmail(), e.getMessage(), e);
            // Don't throw - allow registration to succeed even if event publishing fails
        }
    }
}
