package dev.mathalama.identityservice.application.dto.verification;

public record VerificationResponse(
    String message,
    Boolean verified) {}
