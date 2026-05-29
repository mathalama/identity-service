package dev.mathalama.identityservice.application.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a JWT token validation request.
 *
 * Sent to the POST /auth/validate endpoint by other microservices to verify
 * an access token and retrieve the associated user's information. Supports
 * tokens with or without the "Bearer " prefix.
 *
 * @param token the JWT access token to validate, optionally prefixed with "Bearer "
 *
 * @see TokenValidationResponse
 * @see dev.mathalama.identityservice.presentation.controller.AuthController#validateToken(TokenValidationRequest)
 */
public record TokenValidationRequest(
    @NotBlank(message = "Token is required")
    String token
) {
}

