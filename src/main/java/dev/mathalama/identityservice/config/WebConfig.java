package dev.mathalama.identityservice.config;

import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final FrontendProperties frontendProperties;

    public WebConfig(FrontendProperties frontendProperties) {
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/*")
                .allowedOrigins(frontendProperties.getUrl())
                .allowedMethods("GET","POST","PUT", "DELETE");
    }
}

