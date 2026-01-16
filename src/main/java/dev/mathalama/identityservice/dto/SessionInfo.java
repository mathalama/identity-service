package dev.mathalama.identityservice.dto;

import java.time.Instant;

public record SessionInfo(
        String sessionId,
        Long userId,
        String username,
        String email,
        Instant createdAt,
        Instant lastAccessedAt,
        int maxInactiveIntervalSeconds
) {}
