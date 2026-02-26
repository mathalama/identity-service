package dev.mathalama.identityservice.infrastructure.service;

import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.application.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationTokenRedisService implements VerificationTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.verification.token-expiry-minutes:30}")
    private long tokenExpiryMinutes;

    @Value("${app.verification.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    private static final String TOKEN_PREFIX = "verify:token:";
    private static final String COOLDOWN_PREFIX = "verify:cooldown:";

    @Override
    public String generateVerificationToken(Users user) {
        // Generate raw token (UUID)
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        // Store token in Redis with expiry
        String key = TOKEN_PREFIX + tokenHash;
        redisTemplate.opsForValue().set(
                key,
                user.getId().toString(),
                tokenExpiryMinutes,
                TimeUnit.MINUTES
        );

        // Invalidate previous token for this user (set cooldown)
        String cooldownKey = COOLDOWN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(
                cooldownKey,
                String.valueOf(System.currentTimeMillis()),
                resendCooldownSeconds,
                TimeUnit.SECONDS
        );

        log.info("Generated verification token for user: {}", user.getUsername());

        // Return raw token (never stored in DB)
        return rawToken;
    }

    @Override
    public boolean verifyToken(String rawToken) {
        try {
            String tokenHash = hashToken(rawToken);
            String key = TOKEN_PREFIX + tokenHash;

            String userId = redisTemplate.opsForValue().get(key);

            if (userId == null) {
                log.warn("Token not found or expired");
                return false;
            }

            log.debug("Token verified for user: {}", userId);
            return true;

        } catch (Exception ex) {
            log.error("Error verifying token", ex);
            return false;
        }
    }

    @Override
    public boolean canResendToken(Users user) {
        try {
            String cooldownKey = COOLDOWN_PREFIX + user.getId();
            String lastSendTime = redisTemplate.opsForValue().get(cooldownKey);

            if (lastSendTime == null) {
                // No previous send, can resend
                return true;
            }

            long timeSinceLastSend = System.currentTimeMillis() - Long.parseLong(lastSendTime);
            long cooldownMs = resendCooldownSeconds * 1000;

            boolean canResend = timeSinceLastSend >= cooldownMs;

            if (!canResend) {
                long remainingSeconds = (cooldownMs - timeSinceLastSend) / 1000;
                log.warn("Resend cooldown active for user: {}. Seconds remaining: {}",
                        user.getId(), remainingSeconds);
            }

            return canResend;

        } catch (Exception ex) {
            log.error("Error checking resend cooldown", ex);
            return false;
        }
    }

    @Override
    public void markTokenAsUsed(String rawToken) {
        try {
            String tokenHash = hashToken(rawToken);
            String key = TOKEN_PREFIX + tokenHash;

            // Delete token from Redis (marks as used by deletion)
            Boolean deleted = redisTemplate.delete(key);

            if (deleted != null && deleted) {
                log.info("Marked verification token as used");
            }

        } catch (Exception ex) {
            log.error("Error marking token as used", ex);
        }
    }

    @Override
    public Users getUserByToken(String rawToken) {
        try {
            String tokenHash = hashToken(rawToken);
            String key = TOKEN_PREFIX + tokenHash;

            String userId = redisTemplate.opsForValue().get(key);

            if (userId == null) {
                log.warn("Token not found or expired");
                return null;
            }

            log.debug("User retrieved from token: {}", userId);
            return null; // Will be fetched by AuthService from DB

        } catch (Exception ex) {
            log.error("Error getting user by token", ex);
            return null;
        }
    }

    @Override
    public String getUserIdByToken(String rawToken) {
        try {
            String tokenHash = hashToken(rawToken);
            String key = TOKEN_PREFIX + tokenHash;

            return redisTemplate.opsForValue().get(key);

        } catch (Exception ex) {
            log.error("Error getting userId by token", ex);
            return null;
        }
    }

    private String hashToken(String token) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = messageDigest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException("SHA-256 algorithm not found", ex);
        }
    }
}
