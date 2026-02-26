package dev.mathalama.identityservice.application.service;

import dev.mathalama.identityservice.domain.entity.Users;

public interface JwtService {
    String generateToken(Users user);
    String getUserIdFromToken(String token);
    boolean validateToken(String token);
}
