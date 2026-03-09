package dev.mathalama.identityservice.presentation.controller;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import dev.mathalama.identityservice.application.dto.auth.AuthResponse;
import dev.mathalama.identityservice.application.dto.auth.CurrentUserDto;
import dev.mathalama.identityservice.application.dto.auth.OAuthProviderDto;
import dev.mathalama.identityservice.application.dto.auth.RefreshTokenRequest;
import dev.mathalama.identityservice.application.dto.auth.SignInRequest;
import dev.mathalama.identityservice.application.dto.auth.SignUpRegister;
import dev.mathalama.identityservice.application.dto.auth.TokenValidationRequest;
import dev.mathalama.identityservice.application.dto.auth.TokenValidationResponse;
import dev.mathalama.identityservice.application.dto.password.ForgotPasswordRequest;
import dev.mathalama.identityservice.application.dto.password.NewPasswordRequest;
import dev.mathalama.identityservice.application.dto.password.ResetPasswordRequest;
import dev.mathalama.identityservice.application.dto.verification.ResendVerificationRequest;
import dev.mathalama.identityservice.application.dto.verification.VerificationResponse;
import dev.mathalama.identityservice.application.dto.verification.VerifyEmailRequest;
import dev.mathalama.identityservice.application.service.AuthService;
import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.application.service.OAuthProviderService;
import dev.mathalama.identityservice.domain.entity.Users;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final OAuthProviderService oauthProviderService;

    public AuthController(AuthService authService, JwtService jwtService, OAuthProviderService oauthProviderService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.oauthProviderService = oauthProviderService;
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

    /**
     * Get the current authenticated user profile.
     *
     * This endpoint returns detailed information about the currently authenticated user,
     * including their ID, username, email, verification status, account state, roles, and
     * account creation time. This is useful for UI components that need to display the
     * current user's information.
     *
     * @return ResponseEntity containing CurrentUserDto with user profile, or 401 Unauthorized
     *         if not authenticated
     *
     * @see CurrentUserDto
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Set<String> roleNames = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.toSet());

        CurrentUserDto dto = new CurrentUserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getEmailVerified(),
                user.getAccountState().toString(),
                roleNames,
                user.getCreatedAt().getTime()
        );

        return ResponseEntity.ok(dto);
    }

    /**
     * Get all OAuth providers linked to the current user account.
     *
     * Returns a list of all authentication methods linked to the user's account,
     * including provider name, associated email, link timestamp, and last login time.
     * This allows users to see all their connected authentication methods and unlink
     * providers they no longer wish to use.
     *
     * @return ResponseEntity containing List of OAuthProviderDto, or 401 Unauthorized
     *         if not authenticated
     *
     * @see OAuthProviderDto
     */
    @GetMapping("/me/providers")
    public ResponseEntity<List<OAuthProviderDto>> getLinkedProviders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OAuthProviderDto> providers = oauthProviderService.getUserProviders(user)
                .stream()
                .map(provider -> new OAuthProviderDto(
                        provider.getId(),
                        provider.getProviderName(),
                        provider.getProviderEmail(),
                        provider.getCreatedAt().toEpochMilli(),
                        provider.getLastLoginAt() != null ? provider.getLastLoginAt().toEpochMilli() : null
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(providers);
    }

    /**
     * Unlink an OAuth provider from the current user's account.
     *
     * Removes a specific OAuth provider (e.g., GOOGLE, GITHUB) from the user's account.
     * A user must maintain at least one authentication method (OAuth provider), so the
     * system prevents unlinking the last remaining provider.
     *
     * @param provider the OAuth provider name to unlink (e.g., "GOOGLE", "GITHUB"),
     *                 case-insensitive
     * @return ResponseEntity with 204 No Content on successful unlink,
     *         400 Bad Request if attempting to unlink the last provider,
     *         404 Not Found if provider is not linked to this account,
     *         401 Unauthorized if not authenticated
     *
     * @see OAuthProviderDto
     */
    @DeleteMapping("/me/providers/{provider}")
    public ResponseEntity<Void> unlinkProvider(@PathVariable String provider) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !(authentication.getPrincipal() instanceof Users user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if user would have no auth method left
        long providerCount = oauthProviderService.countLinkedProviders(user.getId());
        if (providerCount <= 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // 400: Can't unlink last method
        }

        boolean unlinked = oauthProviderService.unlinkOAuthProvider(user.getId(), provider.toUpperCase());
        
        if (!unlinked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404: Provider not linked
        }

        return ResponseEntity.noContent().build();
    }

    /**
     * Validate a JWT token and return the associated user information.
     *
     * This is a service-to-service endpoint used by other microservices (Account Service,
     * Transfer Service, etc.) to validate access tokens and retrieve the authenticated
     * user's information. The endpoint handles token format variations (with or without
     * "Bearer " prefix) and returns detailed user data for authorization decisions.
     *
     * @param request TokenValidationRequest containing the JWT token to validate
     * @return ResponseEntity with TokenValidationResponse containing validation result,
     *         user details on success (id, username, email, roles), or error message
     *         on failure. Always returns HTTP 200 OK with success/failure flag in response body.
     *
     * @see TokenValidationRequest
     * @see TokenValidationResponse
     */
    @PostMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@Valid @RequestBody TokenValidationRequest request) {
        try {
            // Extract token (remove "Bearer " prefix if present)
            String token = request.token();
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Validate token and extract user
            Users user = jwtService.validateTokenAndExtractUser(token);
            
            if (user == null) {
                return ResponseEntity.ok(TokenValidationResponse.failure("Invalid or expired token"));
            }

            // Build role set
            Set<String> roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());

            return ResponseEntity.ok(TokenValidationResponse.success(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    roles
            ));

        } catch (Exception e) {
            return ResponseEntity.ok(TokenValidationResponse.failure("Token validation failed: " + e.getMessage()));
        }
    }

}
