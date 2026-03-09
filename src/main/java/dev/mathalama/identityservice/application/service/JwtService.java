package dev.mathalama.identityservice.application.service;

import dev.mathalama.identityservice.domain.entity.Users;

/**
 * Service interface for JWT token operations.
 *
 * Manages creation, validation, storage, revocation, and blacklisting of JWT tokens.
 * Supports both access tokens (short-lived) and refresh tokens (long-lived),
 * with Redis-backed storage for refresh tokens and blacklist management.
 */
public interface JwtService {
    
    /**
     * Generate a new JWT access token for the user.
     *
     * @param user the user for whom to generate the token
     * @return JWT access token string (HMAC-SHA256 signed)
     */
    String generateAccessToken(Users user);
    
    /**
     * Generate a new JWT refresh token for the user.
     *
     * Refresh tokens are stored in Redis with the token ID for later validation.
     * Used to obtain new access tokens without requiring re-authentication.
     *
     * @param user the user for whom to generate the token
     * @return JWT refresh token string (HMAC-SHA256 signed)
     */
    String generateRefreshToken(Users user);
    
    /**
     * Extract user ID from a JWT token.
     *
     * Parses the token and extracts the subject claim, which contains the user UUID.
     * Does not validate token expiration or signature.
     *
     * @param token the JWT token
     * @return the user ID string, or null if not found
     */
    String getUserIdFromToken(String token);
    
    /**
     * Validate a JWT access token.
     *
     * Checks token signature, expiration, format, and blacklist status.
     * Does not validate that the user still exists in the database.\n     *\n     * @param token the JWT token to validate\n     * @return true if token is valid and not expired or blacklisted
     */
    boolean validateToken(String token);
    
    /**
     * Store a refresh token in Redis for validation purposes.\n     *\n     * Stores the token ID with the user ID as key. Used to track valid refresh tokens\n     * and enable token revocation.\n     *\n     * @param userId the user ID as string\n     * @param tokenId the unique token ID from the JWT\n     */
    void storeRefreshToken(String userId, String tokenId);
    
    /**
     * Validate a stored refresh token.\n     *\n     * Checks if the token ID matches the stored value in Redis for this user.\n     * Used when refreshing access tokens to prevent use of revoked refresh tokens.\n     *\n     * @param userId the user ID as string\n     * @param tokenId the unique token ID from the JWT\n     * @return true if stored token matches the provided tokenId\n     */
    boolean validateRefreshToken(String userId, String tokenId);
    
    /**\n     * Revoke all refresh tokens for a user.\n     *\n     * Called on logout to prevent further token refreshes.\n     * Deletes the refresh token record from Redis.\n     *\n     * @param userId the user ID as string\n     */
    void revokeRefreshToken(String userId);
    
    /**\n     * Extract the unique token ID from a JWT token.\n     *\n     * The token ID (jti claim) uniquely identifies this token instance.\n     * Useful for tracking which specific token was revoked or blacklisted.\n     *\n     * @param token the JWT token\n     * @return the token ID string, or null if not found\n     */
    String getTokenId(String token);
    
    /**\n     * Add an access token to the blacklist.\n     *\n     * Called on logout to prevent token reuse even if signature is valid.\n     * Stores token ID in Redis blacklist with expiration equal to token lifespan.\n     *\n     * @param token the JWT access token to blacklist\n     */
    void blacklistAccessToken(String token);
    
    /**\n     * Check if an access token has been blacklisted.\n     *\n     * @param tokenId the unique token ID from the JWT\n     * @return true if token is in the blacklist, false otherwise\n     */
    boolean isAccessTokenBlacklisted(String tokenId);
    
    /**\n     * Get remaining expiration time for a token.\n     *\n     * @param token the JWT token\n     * @return milliseconds until token expiration, or -1 if already expired\n     */
    long getRemainingExpiration(String token);
    
    /**
     * Validate a JWT token and extract the associated user entity.\n     *\n     * Performs comprehensive token validation and loads the full User from database.\n     * Useful for inter-service communication where token validation must also\n     * verify user still exists and retrieve complete user information including roles.\n     *\n     * @param token JWT access token to validate\n     * @return the Users entity if token is valid and user exists, null otherwise\n     *         Returns null if: token invalid, expired, blacklisted, or user not found\n     */
    Users validateTokenAndExtractUser(String token);
}
