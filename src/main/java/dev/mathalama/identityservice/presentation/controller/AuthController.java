package dev.mathalama.identityservice.presentation.controller;

import dev.mathalama.identityservice.application.dto.request.SignUpRequest;
import dev.mathalama.identityservice.application.dto.request.SignInRequest;
import dev.mathalama.identityservice.application.dto.response.AuthResponse;
import dev.mathalama.identityservice.application.dto.response.MessageResponse;
import dev.mathalama.identityservice.domain.model.User;
import dev.mathalama.identityservice.domain.port.in.AuthUseCase;
import dev.mathalama.identityservice.domain.port.out.TokenStore;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final TokenStore tokenStore;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse register(@Valid @RequestBody SignUpRequest request) {
        authUseCase.register(request.username(), request.email(), request.password());
        return new MessageResponse("User registered successfully. Please check your email to verify your account.");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(@Valid @RequestBody SignInRequest request) {
        User user = authUseCase.authenticate(request);
        String accessToken = tokenStore.generateAccessToken(user);
        String refreshToken = tokenStore.generateRefreshToken(user);
        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            String accessToken = authHeader.substring(7);
            authUseCase.logout(user.getId().toString(), accessToken);
        }
        return ResponseEntity.noContent().build();
    }
}
