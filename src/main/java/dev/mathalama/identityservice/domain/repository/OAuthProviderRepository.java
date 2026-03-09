package dev.mathalama.identityservice.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import dev.mathalama.identityservice.domain.entity.OAuthProvider;
import dev.mathalama.identityservice.domain.entity.Users;

@Repository
public interface OAuthProviderRepository extends JpaRepository<OAuthProvider, UUID> {

    /**
     * Find OAuth provider by provider name and provider ID
     * @param providerName e.g. "GOOGLE", "GITHUB"
     * @param providerId the unique ID from OAuth provider
     * @return the OAuth provider if found
     */
    Optional<OAuthProvider> findByProviderNameAndProviderId(String providerName, String providerId);

    /**
     * Find all OAuth providers for a specific user
     * @param user the user
     * @return list of OAuth providers linked to this user
     */
    List<OAuthProvider> findByUser(Users user);

    /**
     * Find OAuth provider by user and provider name
     * @param user the user
     * @param providerName e.g. "GOOGLE", "GITHUB"
     * @return the OAuth provider if found
     */
    Optional<OAuthProvider> findByUserAndProviderName(Users user, String providerName);

    /**
     * Find OAuth provider by user ID and provider name
     * @param userId the user ID
     * @param providerName e.g. "GOOGLE", "GITHUB"
     * @return the OAuth provider if found
     */
    Optional<OAuthProvider> findByUser_IdAndProviderName(UUID userId, String providerName);

    /**
     * Get list of provider names linked to a user
     * @param userId the user ID
     * @return list of provider names (e.g., ["GOOGLE", "GITHUB"])
     */
    List<String> findProviderNamesByUserId(UUID userId);

    /**
     * Count how many different providers are linked to a user
     * @param userId the user ID
     * @return count of providers
     */
    long countByUser_Id(UUID userId);

    /**
     * Delete OAuth provider by user ID and provider name
     * @param userId the user ID
     * @param providerName e.g. "GOOGLE", "GITHUB"
     */
    void deleteByUser_IdAndProviderName(UUID userId, String providerName);
}
