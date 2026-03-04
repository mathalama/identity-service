package dev.mathalama.identityservice.presentation.controller;

import dev.mathalama.identityservice.application.dto.*;
import dev.mathalama.identityservice.application.service.AuthService;
import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.domain.entity.Users;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody SignUpRegister request) {
        authService.register(
                request.username(),
                request.email(),
                request.password()
        );
        return Map.of("message", "User registered successfully. Please check your email to verify your account.");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody SignInRequest request) {
        Users user = authService.authenticate(request);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Users user) {
            String accessToken = authHeader.substring(7); // strip "Bearer "
            authService.logout(user.getId().toString(), accessToken);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerificationResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        VerificationResponse response = authService.verifyEmail(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationResponse> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        VerificationResponse response = authService.resendVerificationEmail(request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        return ResponseEntity.ok(Map.of("message", "If the email exists, a password reset link has been sent."));
    }

    @PostMapping("/reset-forgotten-password")
    public ResponseEntity<Map<String, String>> resetForgottenPassword(@Valid @RequestBody NewPasswordRequest request) {
        authService.resetForgottenPassword(request.token(), request.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password has been reset successfully. Please log in with your new password."));
    }
}
