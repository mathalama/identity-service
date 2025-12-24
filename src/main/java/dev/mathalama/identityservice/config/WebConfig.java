package dev.mathalama.identityservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    public void addCorsMapping(CorsRegistry registry) {
        registry.addMapping("/*")
                .allowedOrigins("https://localhost:5173")
                .allowedMethods("GET","POST","PUT", "DELETE");
    }
}
