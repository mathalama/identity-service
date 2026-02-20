package dev.mathalama.identityservice.dto;

import dev.mathalama.identityservice.entity.Users;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username
) {
    public static UserResponse from (Users user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername()
        );
    }
}
