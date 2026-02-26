package dev.mathalama.identityservice.application.service;

import dev.mathalama.identityservice.domain.entity.Users;

public interface VerificationTokenService {
    String generateVerificationToken(Users user);
    boolean verifyToken(String token);
    boolean canResendToken(Users user);
    void markTokenAsUsed(String token);
    Users getUserByToken(String token);
    String getUserIdByToken(String token);
}
