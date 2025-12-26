package dev.mathalama.identityservice.dto;

public record UpdateRequest (
        String username,
        String email,
        String password
) {
}
