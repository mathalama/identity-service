package dev.mathalama.identityservice.application.service;

public interface EmailService {
    void sendVerificationEmail(String email, String username, String verificationToken);
}
