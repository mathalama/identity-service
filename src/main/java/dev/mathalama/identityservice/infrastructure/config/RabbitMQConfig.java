package dev.mathalama.identityservice.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration for event-driven microservices
 */
@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String USER_EVENTS_EXCHANGE = "user-events-exchange";

    // Queue names
    public static final String USER_REGISTERED_QUEUE = "user-registered-queue";

    // Routing keys
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";

    /**
     * Topic Exchange for user events
     * This allows multiple services to subscribe to user events
     */
    @Bean
    public TopicExchange userEventsExchange() {
        return new TopicExchange(USER_EVENTS_EXCHANGE, true, false);
    }

    /**
     * Queue for user registered events
     * Services like account-service, transfer-service can listen to this
     */
    @Bean
    public Queue userRegisteredQueue() {
        return new Queue(USER_REGISTERED_QUEUE, true, false, false);
    }

    /**
     * Binding between queue and exchange with routing key
     */
    @Bean
    public Binding userRegisteredBinding(Queue userRegisteredQueue, TopicExchange userEventsExchange) {
        return BindingBuilder
                .bind(userRegisteredQueue)
                .to(userEventsExchange)
                .with(USER_REGISTERED_ROUTING_KEY);
    }

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate for sending messages
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}
