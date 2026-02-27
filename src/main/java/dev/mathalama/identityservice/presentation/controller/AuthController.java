package dev.mathalama.identityservice.presentation.controller;

import dev.mathalama.identityservice.application.dto.*;
import dev.mathalama.identityservice.application.service.AuthService;
import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.domain.entity.Users;
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
    public Map<String, String> register(@RequestBody SignUpRegister request) {
        authService.register(
                request.username(),
                request.email(),
                request.password()
        );
        return Map.of("message", "User registered successfully. Please check your email to verify your account.");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@RequestBody SignInRequest request) {
        Users user = authService.authenticate(request);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
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
    public ResponseEntity<VerificationResponse> verifyEmail(@RequestBody VerifyEmailRequest request) {
        VerificationResponse response = authService.verifyEmail(request.token());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<VerificationResponse> resendVerification(@RequestBody ResendVerificationRequest request) {
        VerificationResponse response = authService.resendVerificationEmail(request.email());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {

        authService.resetPassword(
                request.username(),
                request.oldPassword(),
                request.newPassword()
        );

        return ResponseEntity.noContent().build();
    }
}
