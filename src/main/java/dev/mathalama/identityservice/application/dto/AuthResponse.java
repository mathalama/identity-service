package dev.mathalama.identityservice.application.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {}
