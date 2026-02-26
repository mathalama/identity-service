package dev.mathalama.identityservice.application.dto;

import jakarta.annotation.Nonnull;

public record VerifyEmailRequest(
    @Nonnull String token
) {}
