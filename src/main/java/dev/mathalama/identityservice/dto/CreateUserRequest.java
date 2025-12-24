package dev.mathalama.identityservice.dto;

public record CreateUserRequest(
        String username,
        String email,
        String password
) {}
