package dev.mathalama.identityservice.dto;

import dev.mathalama.identityservice.dto.enums.AccountState;
import dev.mathalama.identityservice.dto.enums.SecurityStatus;
import dev.mathalama.identityservice.entity.Users;
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
