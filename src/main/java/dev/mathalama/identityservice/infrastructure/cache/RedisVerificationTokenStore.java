package dev.mathalama.identityservice.infrastructure.cache;

import dev.mathalama.identityservice.domain.model.User;
import dev.mathalama.identityservice.domain.port.out.VerificationTokenStore;
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
public class RedisVerificationTokenStore implements VerificationTokenStore {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.verification.token-expiry-minutes}")
    private long tokenExpiryMinutes;

    @Value("${app.verification.resend-cooldown-seconds}")
    private long resendCooldownSeconds;

    private static final String TOKEN_PREFIX = "verify:token:";
    private static final String COOLDOWN_PREFIX = "verify:cooldown:";

    @Override
    public String generateVerificationToken(User user) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawToken);

        String key = TOKEN_PREFIX + tokenHash;
        redisTemplate.opsForValue().set(
                key,
                user.getId().toString(),
                tokenExpiryMinutes,
                TimeUnit.MINUTES
        );

        String cooldownKey = COOLDOWN_PREFIX + user.getId();
        redisTemplate.opsForValue().set(
                cooldownKey,
                String.valueOf(System.currentTimeMillis()),
                resendCooldownSeconds,
                TimeUnit.SECONDS
        );

        log.info("Generated verification token for user: {}", user.getUsername());

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
    public boolean canResendToken(User user) {
        try {
            String cooldownKey = COOLDOWN_PREFIX + user.getId();
            String lastSendTime = redisTemplate.opsForValue().get(cooldownKey);

            if (lastSendTime == null) {
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

            Boolean deleted = redisTemplate.delete(key);

            if (deleted != null && deleted) {
                log.info("Marked verification token as used");
            }

        } catch (Exception ex) {
            log.error("Error marking token as used", ex);
        }
    }

    @Override
    public User getUserByToken(String rawToken) {
        try {
            String tokenHash = hashToken(rawToken);
            String key = TOKEN_PREFIX + tokenHash;

            String userId = redisTemplate.opsForValue().get(key);

            if (userId == null) {
                log.warn("Token not found or expired");
                return null;
            }

            log.debug("User retrieved from token: {}", userId);
            return null; // Will be fetched by UseCase from DB

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
