package dev.mathalama.identityservice.infrastructure.email;

import dev.mathalama.identityservice.domain.port.out.EmailSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${app.mail.username}")
    private String fromEmail;

    @Override
    @Async("taskExecutor")
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        try {
            String verificationLink = String.format(
                "%s/verify-email?token=%s",
                frontendUrl,
                verificationToken
            );

            log.info("Sending verification email to: {}", email);
            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send verification email to: {}", email, ex);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String email, String username, String resetToken) {
        try {
            String resetLink = String.format(
                "%s/reset-password?token=%s",
                frontendUrl,
                resetToken
            );

            log.info("Sending password reset email to: {}", email);
            log.info("Password reset email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send password reset email to: {}", email, ex);
        }
    }
}
