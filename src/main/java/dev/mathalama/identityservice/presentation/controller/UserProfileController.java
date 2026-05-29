package dev.mathalama.identityservice.presentation.controller;

import dev.mathalama.identityservice.application.dto.response.CurrentUserResponse;
import dev.mathalama.identityservice.application.dto.response.OAuthProviderResponse;
import dev.mathalama.identityservice.application.mapper.UserMapper;
import dev.mathalama.identityservice.application.mapper.OAuthProviderMapper;
import dev.mathalama.identityservice.domain.model.User;
import dev.mathalama.identityservice.domain.port.in.OAuthProviderUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the current user's profile and linked OAuth providers.
 *
 * <p>All endpoints require authentication and operate on the currently
 * authenticated user obtained from {@link SecurityContextHolder}.</p>
 */
@RestController
@RequestMapping("/auth/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final OAuthProviderUseCase oAuthProviderUseCase;

    /**
     * Get the current authenticated user profile.
     *
     * @return ResponseEntity containing CurrentUserResponse with user profile,
     *         or 401 Unauthorized if not authenticated
     */
    @GetMapping("")
    public ResponseEntity<CurrentUserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(UserMapper.toCurrentUserResponse(user));
    }

    /**
     * Get all OAuth providers linked to the current user account.
     *
     * @return ResponseEntity containing List of OAuthProviderResponse,
     *         or 401 Unauthorized if not authenticated
     */
    @GetMapping("/providers")
    public ResponseEntity<List<OAuthProviderResponse>> getLinkedProviders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<OAuthProviderResponse> providers = oAuthProviderUseCase.getUserProviders(user)
                .stream()
                .map(OAuthProviderMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(providers);
    }

    /**
     * Unlink an OAuth provider from the current user's account.
     *
     * @param provider the OAuth provider name to unlink (e.g., "GOOGLE", "GITHUB")
     * @return ResponseEntity with 204 No Content on success,
     *         400 Bad Request if attempting to unlink the last provider,
     *         404 Not Found if provider is not linked,
     *         401 Unauthorized if not authenticated
     */
    @DeleteMapping("/providers/{provider}")
    public ResponseEntity<Void> unlinkProvider(@PathVariable String provider) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if user would have no auth method left
        long providerCount = oAuthProviderUseCase.countLinkedProviders(user.getId());
        if (providerCount <= 1) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        boolean unlinked = oAuthProviderUseCase.unlinkOAuthProvider(user.getId(), provider.toUpperCase());

        if (!unlinked) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.noContent().build();
    }
}
