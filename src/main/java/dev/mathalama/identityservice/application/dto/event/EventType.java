package dev.mathalama.identityservice.application.dto.event;

import lombok.Getter;

@Getter
public enum EventType {

    USER_REGISTERED("user-registered-topic");

    private final String topic;

    EventType(String topic) {
        this.topic = topic;
    }
}