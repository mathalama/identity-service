package dev.mathalama.identityservice.dto;

public record SignUpRegister(
        String username,
        String email,
        String password
) {}
