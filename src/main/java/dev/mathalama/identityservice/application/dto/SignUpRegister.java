package dev.mathalama.identityservice.application.dto;

import jakarta.annotation.Nonnull;

public record SignUpRegister(
        @Nonnull String username,
        @Nonnull String email,
        @Nonnull String password
) {}
