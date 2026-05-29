package dev.mathalama.identityservice.infrastructure.security.filter;

import dev.mathalama.identityservice.domain.model.User;
import dev.mathalama.identityservice.domain.port.out.UserRepository;
import dev.mathalama.identityservice.domain.port.out.TokenStore;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenStore tokenStore;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(TokenStore tokenStore, UserRepository userRepository) {
        this.tokenStore = tokenStore;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && tokenStore.validateToken(token)) {
                String userId = tokenStore.getUserIdFromToken(token);
                if (userId != null) {
                    User user = userRepository.findById(UUID.fromString(userId)).orElse(null);
                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated user: {}", user.getUsername());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
