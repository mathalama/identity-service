package dev.mathalama.identityservice.dto;

import jakarta.annotation.Nonnull;

public record SignInRequest(
        @Nonnull String login,
        @Nonnull String password
) {}
