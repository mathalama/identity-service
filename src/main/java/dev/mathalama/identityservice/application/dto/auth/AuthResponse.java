package dev.mathalama.identityservice.application.dto.auth;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
