package dev.mathalama.identityservice.application.dto.response;

import java.util.UUID;

/**
 * DTO representing an OAuth provider linked to a user account.
 *
 * Returned by GET /auth/me/providers endpoint, this record represents a single
 * OAuth authentication method (e.g., Google, GitHub) that has been linked to
 * the user's account. Includes linking timestamp and last successful login time.
 *
 * @param id the unique identifier of this provider link
 * @param providerName the OAuth provider name (GOOGLE, GITHUB, etc.)
 * @param providerEmail the email address associated with this provider
 * @param linkedAt timestamp when this provider was linked (milliseconds since epoch)
 * @param lastLoginAt timestamp of the last login via this provider,
     *                    or null if provider has never been used to login
 *
 * @see dev.mathalama.identityservice.presentation.controller.AuthController#getLinkedProviders()
 */
public record OAuthProviderResponse(
    UUID id,
    String providerName,
    String providerEmail,
    Long linkedAt,
    Long lastLoginAt
) {
}

