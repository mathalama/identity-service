package dev.mathalama.identityservice.application.dto;

import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.domain.enums.AccountState;
import dev.mathalama.identityservice.domain.enums.SecurityStatus;

import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String username,
        AccountState accountState,
        SecurityStatus securityStatus,
        List<String> roles
) {
    public static UserResponse from(Users user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAccountState(),
                user.getSecurityStatus(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName())
                        .toList()
        );
    }
}
