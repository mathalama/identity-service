package dev.mathalama.identityservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                                .expiredUrl("/login")
                        )
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/**").permitAll()
                        .requestMatchers("/user/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                )
                .httpBasic(withDefaults());

        return http.build();
    }
}
