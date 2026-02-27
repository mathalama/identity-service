package dev.mathalama.identityservice.application.dto;

import jakarta.annotation.Nonnull;

public record RefreshTokenRequest(
        @Nonnull String refreshToken
) {}
