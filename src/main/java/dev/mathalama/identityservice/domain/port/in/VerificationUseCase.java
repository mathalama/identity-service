package dev.mathalama.identityservice.domain.port.in;

import dev.mathalama.identityservice.application.dto.response.VerificationResponse;

public interface VerificationUseCase {
    VerificationResponse verifyEmail(String token);
    VerificationResponse resendVerificationEmail(String email);
}
