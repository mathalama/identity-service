package dev.mathalama.identityservice.application.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mathalama.identityservice.domain.entity.OAuthProvider;
import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.domain.repository.OAuthProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing OAuth2 provider connections.
 * 
 * Handles linking/unlinking OAuth providers to user accounts,
 * finding users by OAuth provider ID, and tracking provider logins.
 * 
 * This enables unified accounts where users can authenticate via
 * email, Google, GitHub, etc., and it's all the same account.
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class OAuthProviderService {

    private final OAuthProviderRepository oauthProviderRepository;

    /**
     * Find a user by OAuth provider credentials.
     *
     * Located during OAuth login flow to check if an account is already linked to
     * this OAuth provider ID. This is the primary key for OAuth provider records.
     *
     * Use case: User logs in with Google, we search for any existing user linked
     * to that Google account ID. If found, authenticate them. If not, proceed to
     * check if email exists or create new account.
     *
     * @param providerName the OAuth provider name ("GOOGLE", "GITHUB", etc.)
     * @param providerId the unique identifier from the OAuth provider
     * @return Optional containing the Users entity if found, empty otherwise
     */
    public Optional<Users> findUserByOAuthProvider(String providerName, String providerId) {
        log.debug("Looking up user by {} provider with ID: {}", providerName, providerId);
        
        return oauthProviderRepository
                .findByProviderNameAndProviderId(providerName, providerId)
                .map(OAuthProvider::getUser);
    }

    /**
     * Link an OAuth provider to an existing user account.
     *
     * Creates a new OAuthProvider record establishing a connection between the user
     * and an OAuth provider. If the same provider is already linked, returns the
     * existing link with updated timestamp.
     *
     * Use case: User has an account and wants to "Connect Google" from settings,
     * or during first login, an OAuth provider is being linked to newly created account.
     *
     * @param user the user to link the provider to
     * @param providerName the OAuth provider name ("GOOGLE", "GITHUB", etc.)
     * @param providerId the unique identifier from the OAuth provider
     * @param providerEmail email address from the OAuth provider
     * @return the newly created or updated OAuthProvider entity
     */
    public OAuthProvider linkOAuthProvider(Users user, String providerName, String providerId, String providerEmail) {
        log.info("Linking {} provider to user: {}", providerName, user.getUsername());
        
        // Check if this provider is already linked to this user
        Optional<OAuthProvider> existing = oauthProviderRepository
                .findByUserAndProviderName(user, providerName);
        
        if (existing.isPresent()) {
            log.warn("Provider {} already linked to user {}", providerName, user.getUsername());
            OAuthProvider provider = existing.get();
            provider.setUpdatedAt(Instant.now());
            return oauthProviderRepository.save(provider);
        }

        OAuthProvider provider = new OAuthProvider(user, providerName, providerId, providerEmail);
        return oauthProviderRepository.save(provider);
    }

    /**
     * Check if a specific OAuth provider is linked to a user.
     *
     * @param user the user to check
     * @param providerName the OAuth provider name ("GOOGLE", "GITHUB", etc.)
     * @return true if this user has this provider linked, false otherwise
     */
    public boolean isProviderLinked(Users user, String providerName) {
        return oauthProviderRepository.findByUserAndProviderName(user, providerName).isPresent();
    }

    /**
     * Get all OAuth providers linked to a user.
     *
     * Returns all authentication methods (OAuth providers) currently connected to the user.
     * Used for displaying "Connected Accounts" in user settings or for security checks.
     *
     * @param user the user to get providers for
     * @return list of OAuthProvider entities linked to this user (may be empty)
     */
    public List<OAuthProvider> getUserProviders(Users user) {
        return oauthProviderRepository.findByUser(user);
    }

    /**
     * Get all provider names linked to a user.
     *
     * Returns just the provider names (not full entities) for quick checks.
     * Useful for UI components showing which providers are available to unlink.
     *
     * @param userId the user ID
     * @return list of provider names (e.g., ["GOOGLE", "GITHUB"])
     */
    public List<String> getLinkedProviderNames(UUID userId) {
        return oauthProviderRepository.findProviderNamesByUserId(userId);
    }

    /**
     * Record a login event for an OAuth provider.
     *
     * Updates the lastLoginAt timestamp for the provider link. Useful for tracking
     * authentication patterns, detecting inactive providers, and analytics.
     *
     * @param user the user who logged in
     * @param providerName the OAuth provider used ("GOOGLE", "GITHUB", etc.)
     */
    public void recordLogin(Users user, String providerName) {
        oauthProviderRepository
                .findByUserAndProviderName(user, providerName)
                .ifPresent(provider -> {
                    provider.recordLogin();
                    oauthProviderRepository.save(provider);
                    log.debug("Recorded login for user {} via {}", user.getUsername(), providerName);
                });
    }

    /**
     * Unlink an OAuth provider from a user account.
     *
     * Removes the connection between the user and an OAuth provider, preventing
     * future logins via that provider. Used when user wants to disconnect their
     * Google/GitHub account from settings.
     *
     * Note: Consider business logic to prevent unlinking the last method of authentication.
     *
     * @param userId the user ID
     * @param providerName the OAuth provider to unlink ("GOOGLE", "GITHUB", etc.)
     * @return true if provider was unlinked, false if provider wasn't linked to this user
     */
    public boolean unlinkOAuthProvider(UUID userId, String providerName) {
        Optional<OAuthProvider> provider = oauthProviderRepository.findByUser_IdAndProviderName(userId, providerName);
        
        if (provider.isPresent()) {
            oauthProviderRepository.deleteByUser_IdAndProviderName(userId, providerName);
            log.info("Unlinked {} provider from user ID: {}", providerName, userId);
            return true;
        }
        
        log.warn("Could not unlink {} provider - not found for user ID: {}", providerName, userId);
        return false;
    }

    /**
     * Check if a provider ID is already registered in the system.
     *
     * Used to prevent duplicate registrations of the same OAuth provider account
     * to ensure provider IDs are globally unique identifiers.
     *
     * @param providerName the OAuth provider name ("GOOGLE", "GITHUB", etc.)
     * @param providerId the unique ID from the provider
     * @return true if this provider ID is already linked to some user, false otherwise
     */
    public boolean providerIdExists(String providerName, String providerId) {
        return oauthProviderRepository
                .findByProviderNameAndProviderId(providerName, providerId)
                .isPresent();
    }

    /**
     * Find user by email from OAuth provider.
     * Used to auto-link accounts if emails match.
     *
     * @param providerEmail email from provider
     * @param userRepository reference to find user by email
     * @return the user if found
     */
    public Optional<OAuthProvider> findByProviderEmail(String providerEmail) {
        // This is a helper - actual query would need to be in repository
        // For now, this is a placeholder for future enhancement
        return Optional.empty();
    }

    /**
     * Count how many different providers a user has linked.
     *
     * Useful for UI components showing "connected accounts" and for business logic
     * that prevents unlinking the last remaining authentication method.
     *
     * @param userId the user ID
     * @return count of OAuth providers linked to this user
     */
    public long countLinkedProviders(UUID userId) {
        return oauthProviderRepository.countByUser_Id(userId);
    }
}
