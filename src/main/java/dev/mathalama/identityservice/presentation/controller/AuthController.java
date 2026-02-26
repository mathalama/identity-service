package dev.mathalama.identityservice.presentation.controller;

import dev.mathalama.identityservice.application.dto.*;
import dev.mathalama.identityservice.application.service.AuthService;
import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.domain.entity.Users;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody SignInRequest request) {
        Users user = authService.authenticate(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
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
}
