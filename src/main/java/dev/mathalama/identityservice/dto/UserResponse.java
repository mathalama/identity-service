package dev.mathalama.identityservice.dto;

import dev.mathalama.identityservice.entity.Users;

public record UserResponse(
        Long id,
        String email,
        String username
) {
    public static UserResponse from (Users user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
