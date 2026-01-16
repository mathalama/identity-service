package dev.mathalama.identityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 3600) // 1 hour session timeout
public class SessionConfig {

    @Bean
    public SessionRepositoryCustomizer sessionRepositoryCustomizer() {
        return sessionRepository -> {
            // Session configuration can be customized here if needed
        };
    }
}
