package dev.mathalama.identityservice.application.dto;

import jakarta.annotation.Nonnull;

public record ResendVerificationRequest(
    @Nonnull String email
) {}
