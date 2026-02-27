package dev.mathalama.identityservice.application.dto;

import jakarta.annotation.Nonnull;

public record ResetPasswordRequest(
        @Nonnull String username,
        @Nonnull String oldPassword,
        @Nonnull String newPassword
) {
}
