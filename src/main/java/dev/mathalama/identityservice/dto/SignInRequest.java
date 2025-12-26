package dev.mathalama.identityservice.dto;

public record SignInRequest(
        String email,
        String password
) {}
