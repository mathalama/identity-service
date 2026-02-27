package dev.mathalama.identityservice.infrastructure.service;

import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.domain.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "refresh:token:";
    private static final String BLACKLIST_PREFIX = "blacklist:access:";

    public JwtServiceImpl(@Value("${jwt.secret}") String secret,
                          @Value("${jwt.expiration}") long accessExpiration,
                          @Value("${jwt.refresh-expiration:604800000}") long refreshExpiration,
                          RedisTemplate<String, String> redisTemplate) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpiration = accessExpiration;
        this.refreshTokenExpiration = refreshExpiration;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public String generateAccessToken(Users user) {
        Map<String, Object> claims = new HashMap<>();

        List<String> rolesList = user.getRoles() != null && !user.getRoles().isEmpty()
                ? user.getRoles().stream()
                    .map(role -> role.getName())
                    .toList()
                : List.of("ROLE_USER");

        claims.put("roles", rolesList);
        claims.put("type", "access");

        return Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer("Identity Service")
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenExpiration))
                .signWith(secretKey)
                .compact();
    }

    @Override
    public String generateRefreshToken(Users user) {
        String tokenId = UUID.randomUUID().toString();

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getId().toString())
                .issuer("Identity Service")
                .id(tokenId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiration))
                .signWith(secretKey)
                .compact();

        // Store refresh token id in Redis for validation & revocation
        storeRefreshToken(user.getId().toString(), tokenId);

        return token;
    }

    @Override
    public void storeRefreshToken(String userId, String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, tokenId, refreshTokenExpiration, TimeUnit.MILLISECONDS);
        log.debug("Stored refresh token for user: {}", userId);
    }

    @Override
    public boolean validateRefreshToken(String userId, String tokenId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        String storedTokenId = redisTemplate.opsForValue().get(key);
        return tokenId != null && tokenId.equals(storedTokenId);
    }

    @Override
    public void revokeRefreshToken(String userId) {
        String key = REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Revoked refresh token for user: {}", userId);
    }

    @Override
    public String getTokenId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getId();
        } catch (ExpiredJwtException e) {
            // Even if expired, we can still read claims for refresh rotation
            return e.getClaims().getId();
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cannot extract token id: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public void blacklistAccessToken(String token) {
        String tokenId = getTokenId(token);
        if (tokenId == null) {
            log.warn("Cannot blacklist token: unable to extract token id");
            return;
        }

        long remainingMs = getRemainingExpiration(token);
        if (remainingMs <= 0) {
            log.debug("Token already expired, no need to blacklist");
            return;
        }

        String key = BLACKLIST_PREFIX + tokenId;
        redisTemplate.opsForValue().set(key, "revoked", remainingMs, TimeUnit.MILLISECONDS);
        log.debug("Access token blacklisted, jti={}, ttl={}ms", tokenId, remainingMs);
    }

    @Override
    public boolean isAccessTokenBlacklisted(String tokenId) {
        String key = BLACKLIST_PREFIX + tokenId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public long getRemainingExpiration(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            Date expiration = claims.getExpiration();
            return expiration.getTime() - System.currentTimeMillis();
        } catch (ExpiredJwtException e) {
            return 0;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Cannot extract expiration: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            String subject = claims.getSubject();
            if (subject == null || subject.isEmpty()) {
                log.warn("Token subject is empty");
                return null;
            }
            return subject;
        } catch (ExpiredJwtException e) {
            log.warn("Token is expired: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Only accept access tokens for API authentication
            String type = claims.get("type", String.class);
            if (!"access".equals(type)) {
                log.warn("Token is not an access token");
                return false;
            }

            // Check if token has been blacklisted (logout / revoke)
            String tokenId = claims.getId();
            if (tokenId != null && isAccessTokenBlacklisted(tokenId)) {
                log.warn("Access token has been revoked, jti={}", tokenId);
                return false;
            }

            log.debug("JWT access token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (JwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }
}
