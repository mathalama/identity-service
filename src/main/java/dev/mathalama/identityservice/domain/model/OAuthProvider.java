package dev.mathalama.identityservice.domain.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * OAuthProvider entity to track OAuth2 provider connections to User.
 * Allows a single user to have multiple OAuth providers (Google, GitHub, etc.) linked to their account.
 *
 * When a user logs in via OAuth, we check if this provider is already linked to the account,
 * or if we need to create a new link. This enables seamless account unification.
 *
 * Example:
 * - User registers with email: john@example.com
 * - Later, user tries to login with Google. We check if Google account email matches OR
 *   if they explicitly confirm the link. If yes, we create OAuthProvider record linking
 *   this Google account to the existing user.
 */
@Entity
@Table(
    name = "oauth_providers",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "provider_name"}, name = "unique_provider_per_user")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OAuthProvider {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * OAuth provider name: GOOGLE, GITHUB, MICROSOFT, etc.
     */
    @Column(name = "provider_name", nullable = false, length = 50)
    private String providerName;

    /**
     * The unique identifier from OAuth provider (e.g., "sub" claim from Google OIDC token).
     * Must be unique per provider, but can be duplicated across different providers.
     */
    @Column(name = "provider_id", nullable = false, length = 500)
    private String providerId;

    /**
     * Email address from the OAuth provider.
     * Useful for verification that emails match before linking accounts.
     */
    @Column(name = "provider_email", length = 255)
    private String providerEmail;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    /**
     * Track when user last logged in via this provider.
     * Useful for analytics and security decisions.
     */
    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    /**
     * Constructor for quick creation
     */
    public OAuthProvider(User user, String providerName, String providerId, String providerEmail) {
        this.user = user;
        this.providerName = providerName;
        this.providerId = providerId;
        this.providerEmail = providerEmail;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Record a login event via this OAuth provider.
     *
     * Updates the lastLoginAt timestamp to the current moment and refreshes updatedAt.
     * Used to track when User last authenticated via each provider for analytics
     * and security purposes (e.g., detecting inactive providers).
     */
    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    /**
     * Check if this provider link is for the specified OAuth provider.
     *
     * @param name the provider name to check (e.g., "GOOGLE", "GITHUB")
     * @return true if this link is for the specified provider, false otherwise
     */
    public boolean isProvider(String name) {
        return this.providerName.equals(name);
    }

    /**
     * Check if this provider link matches the given OAuth provider ID.
     *
     * Used to verify that a provider ID received from OAuth authorization matches
     * a stored provider link (e.g., verifying Google's "sub" claim matches).
     *
     * @param id the provider-specific ID to match against
     * @return true if this link's providerId equals the given ID, false otherwise
     */
    public boolean matchesProviderId(String id) {
        return this.providerId.equals(id);
    }

    /**
     * Check if the provider email matches the given email.
     *
     * Used during account linking to verify that the email from the OAuth provider
     * matches the user's existing email, ensuring account consolidation is intentional.
     *
     * @param otherEmail the email address to compare against
     * @return true if this link's providerEmail equals the given email, false otherwise
     *         (returns false if providerEmail is null)
     */
    public boolean emailMatches(String otherEmail) {
        return this.providerEmail != null && this.providerEmail.equals(otherEmail);
    }
}
