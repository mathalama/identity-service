package dev.mathalama.identityservice.application.dto.auth;

import java.util.Set;
import java.util.UUID;

/**
 * DTO representing the result of JWT token validation.
 *
 * Returned by the POST /auth/validate endpoint, this record indicates whether
 * a JWT token is valid and, if valid, provides the associated user's information.
 * This is used for inter-service authentication in the microservices architecture.
 *
 * Always include either user details (on success) or a descriptive error message.
 * HTTP 200 OK is always returned; check the 'valid' flag in the response body
 * to determine actual validation result.
 *
 * @param valid true if token is valid and user exists, false otherwise
 * @param userId the user's unique identifier if valid, null otherwise
 * @param username the user's username if valid, null otherwise
 * @param email the user's email address if valid, null otherwise
 * @param roles set of user's role names if valid, empty set otherwise
 * @param message descriptive message about validation result
 *
 * @see TokenValidationRequest
 * @see dev.mathalama.identityservice.presentation.controller.AuthController#validateToken(TokenValidationRequest)
 */
public record TokenValidationResponse(
    Boolean valid,
    UUID userId,
    String username,
    String email,
    Set<String> roles,
    String message
) {
    
    /**
     * Create a successful token validation response.
     *
     * Factory method for constructing a TokenValidationResponse that indicates
     * a token is valid and includes the user's information.
     *
     * @param userId the user's unique identifier
     * @param username the user's username
     * @param email the user's email address
     * @param roles the set of user's role names
     * @return TokenValidationResponse with valid=true and user details
     */
    public static TokenValidationResponse success(UUID userId, String username, String email, Set<String> roles) {
        return new TokenValidationResponse(true, userId, username, email, roles, "Token is valid");
    }
    
    /**
     * Create a failed token validation response.
     *
     * Factory method for constructing a TokenValidationResponse that indicates
     * token validation failed. All user-related fields are set to null/empty.
     *
     * @param message descriptive error message explaining why validation failed
     *                (e.g., "Invalid or expired token", "User not found")
     * @return TokenValidationResponse with valid=false and provided error message
     */
    public static TokenValidationResponse failure(String message) {
        return new TokenValidationResponse(false, null, null, null, Set.of(), message);
    }
}
