package dev.mathalama.identityservice.application.dto;

public record VerificationResponse(
    String message,
    Boolean verified) {}
