package dev.mathalama.identityservice.infrastructure.email;

import dev.mathalama.identityservice.domain.port.out.EmailSender;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmtpEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("classpath:templates/verification-email.html")
    private Resource verificationTemplateResource;

    @Value("classpath:templates/reset-password-email.html")
    private Resource resetPasswordTemplateResource;

    @Override
    @Async("taskExecutor")
    public void sendVerificationEmail(String email, String username, String verificationToken) {
        try {
            String verificationLink = String.format("%s/verify-email?token=%s", frontendUrl, verificationToken);
            log.info("Sending verification email to: {}", email);

            String template = new String(verificationTemplateResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String htmlContent = String.format(template, username, verificationLink, verificationLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Identity Service");
            helper.setTo(email);
            helper.setSubject("Verify your email address");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verification email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send verification email to: {}", email, ex);
        }
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String email, String username, String resetToken) {
        try {
            String resetLink = String.format("%s/reset-password?token=%s", frontendUrl, resetToken);
            log.info("Sending password reset email to: {}", email);

            String template = new String(resetPasswordTemplateResource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            String htmlContent = String.format(template, username, resetLink, resetLink);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "Identity Service");
            helper.setTo(email);
            helper.setSubject("Password Reset Request");
            helper.setText(htmlContent, true);

            mailSender.send(message);

            log.info("Password reset email sent successfully to: {}", email);

        } catch (Exception ex) {
            log.error("Failed to send password reset email to: {}", email, ex);
        }
    }
}