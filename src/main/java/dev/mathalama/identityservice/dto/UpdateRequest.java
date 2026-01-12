package dev.mathalama.identityservice.dto;

import jakarta.annotation.Nonnull;

public record UpdateRequest (
        @Nonnull String username,
        @Nonnull String email,
        @Nonnull String password
) {
}
