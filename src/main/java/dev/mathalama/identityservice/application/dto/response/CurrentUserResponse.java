package dev.mathalama.identityservice.application.dto.response;

import java.util.Set;
import java.util.UUID;

/**
 * DTO representing the current authenticated user's profile information.
 *
 * This record is returned by the GET /auth/me endpoint and contains essential
 * user profile data including authentication status, roles, and account state.
 * All timestamps are in milliseconds since epoch (compatible with JavaScript Date).
 *
 * @param id the user's unique identifier (UUID)
 * @param username the user's login username
 * @param email the user's email address
 * @param emailVerified whether the email has been verified
 * @param accountState current account state (ACTIVE, SUSPENDED, LOCKED, etc.)
 * @param roles set of role names assigned to the user
 * @param createdAt account creation timestamp in milliseconds since epoch
 *
 * @see dev.mathalama.identityservice.presentation.controller.AuthController#getCurrentUser()
 */
public record CurrentUserResponse(
    UUID id,
    String username,
    String email,
    Boolean emailVerified,
    String accountState,
    Set<String> roles,
    Long createdAt
) {
}

