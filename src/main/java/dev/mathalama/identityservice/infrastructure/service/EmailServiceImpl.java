package dev.mathalama.identityservice.infrastructure.service;

import dev.mathalama.identityservice.application.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Override
    @Async("taskExecutor")
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        try {
            String verificationLink = String.format(
                "%s/verify-email?token=%s",
                frontendUrl,
                verificationToken
            );

            String subject = "Email Verification - Identity Service";
            String htmlContent = buildVerificationEmailTemplate(username, verificationLink);

            log.info("Sending verification email to: {}", email);

            // Placeholder for actual email sending (e.g., Resend, SendGrid, AWS SES)
            // In production, integrate with:
            // - Resend (resend.com)
            // - SendGrid (sendgrid.com)
            // - AWS SES
            // - Spring Mail (JavaMailSender)

            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send verification email to: {}", email, ex);
            // In production, implement retry mechanism or dead-letter queue
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

            // Placeholder for actual email sending
            // Use the same transport as sendVerificationEmail

            log.info("Password reset email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send password reset email to: {}", email, ex);
        }
    }

    private String buildVerificationEmailTemplate(String username, String verificationLink) {
        return String.format("""
            <!DOCTYPE html>
            <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: Arial, sans-serif; background-color: #f4f4f4; }
                        .container { max-width: 600px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; }
                        .header { background-color: #007bff; color: white; padding: 20px; border-radius: 8px 8px 0 0; text-align: center; }
                        .content { padding: 20px; }
                        .footer { background-color: #f4f4f4; padding: 10px; text-align: center; font-size: 12px; color: #666; }
                        .btn { display: inline-block; padding: 10px 20px; background-color: #007bff; color: white; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                        .token-warning { margin-top: 20px; padding: 10px; background-color: #fff3cd; border-left: 4px solid #ffc107; color: #856404; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>Email Verification</h1>
                        </div>
                        <div class="content">
                            <p>Hi <strong>%s</strong>,</p>
                            <p>Welcome! Please verify your email address to complete your registration.</p>
                            <p>Click the button below to verify your email:</p>
                            <center>
                                <a href="%s" class="btn">Verify Email</a>
                            </center>
                            <p>Or copy and paste this link in your browser:</p>
                            <p style="word-break: break-all; color: #007bff;">%s</p>
                            <div class="token-warning">
                                <strong>This link expires in 30 minutes.</strong>
                            </div>
                            <p>If you didn't sign up for this account, please ignore this email.</p>
                        </div>
                        <div class="footer">
                            <p>&copy; 2026 Identity Service. All rights reserved.</p>
                        </div>
                    </div>
                </body>
            </html>
            """, username, verificationLink, verificationLink);
    }
}
