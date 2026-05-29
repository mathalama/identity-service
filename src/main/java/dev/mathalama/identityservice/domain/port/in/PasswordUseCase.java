package dev.mathalama.identityservice.domain.port.in;

public interface PasswordUseCase {
    void resetPassword(String username, String oldPassword, String newPassword);
    void forgotPassword(String email);
    void resetForgottenPassword(String token, String newPassword);
}
