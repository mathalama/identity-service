package dev.mathalama.identityservice.application.service;

import dev.mathalama.identityservice.domain.entity.Users;

public interface JwtService {
    String generateAccessToken(Users user);
    String generateRefreshToken(Users user);
    String getUserIdFromToken(String token);
    boolean validateToken(String token);
    void storeRefreshToken(String userId, String tokenId);
    boolean validateRefreshToken(String userId, String tokenId);
    void revokeRefreshToken(String userId);
    String getTokenId(String token);
    void blacklistAccessToken(String token);
    boolean isAccessTokenBlacklisted(String tokenId);
    long getRemainingExpiration(String token);
}
