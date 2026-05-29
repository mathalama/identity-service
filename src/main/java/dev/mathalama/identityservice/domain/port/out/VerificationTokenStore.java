package dev.mathalama.identityservice.domain.port.out;

import dev.mathalama.identityservice.domain.model.User;

public interface VerificationTokenStore {
    String generateVerificationToken(User user);
    boolean verifyToken(String token);
    boolean canResendToken(User user);
    void markTokenAsUsed(String token);
    User getUserByToken(String token);
    String getUserIdByToken(String token);
}
