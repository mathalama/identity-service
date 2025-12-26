package dev.mathalama.identityservice.dto;

import jakarta.annotation.Nonnull;

public record ResetPasswordRequest (
        @Nonnull String login,
        @Nonnull String password
){
}
