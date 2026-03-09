package dev.mathalama.identityservice.infrastructure.config;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import dev.mathalama.identityservice.application.service.EventPublisher;
import dev.mathalama.identityservice.application.service.JwtService;
import dev.mathalama.identityservice.application.service.OAuthProviderService;
import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.domain.enums.AccountState;
import dev.mathalama.identityservice.infrastructure.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2SuccessHandler.class);
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final EventPublisher eventPublisher;
    private final OAuthProviderService oauthProviderService;

    public OAuth2SuccessHandler(UserRepository userRepository, 
                               JwtService jwtService, 
                               EventPublisher eventPublisher,
                               OAuthProviderService oauthProviderService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.eventPublisher = eventPublisher;
        this.oauthProviderService = oauthProviderService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("=== OAuth2 Authentication Success ===");
        
        if (authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            Map<String, Object> attributes = oauth2User.getAttributes();
            String email = (String) attributes.get("email");
            String name = (String) attributes.get("name");
            String providerId = extractUniqueIdentifier(attributes);
            String providerName = getAuthProviderName(request);
            
            logger.info("Email: {}", email);
            logger.info("Name: {}", name);
            logger.info("Provider ID: {}", providerId);
            logger.info("Provider: {}", providerName);
            
            Users user = null;
            boolean isNewUser = false;
            
            // 1. Проверить: не связан ли этот провайдер уже с существующим аккаунтом?
            var existingUser = oauthProviderService.findUserByOAuthProvider(providerName, providerId);
            
            if (existingUser.isPresent()) {
                // Этот провайдер уже связан с аккаунтом
                user = existingUser.get();
                logger.info("User found by {} provider ID", providerName);
            } else {
                // 2. Провайдер не связан. Проверить: есть ли пользователь с такой email?
                var userByEmail = userRepository.findByEmail(email);
                
                if (userByEmail.isPresent()) {
                    // Email найден - связываем этот провайдер с существующим аккаунтом
                    user = userByEmail.get();
                    logger.info("User found by email. Linking {} provider", providerName);
                    oauthProviderService.linkOAuthProvider(user, providerName, providerId, email);
                } else {
                    // 3. Полностью новый пользователь - создаём аккаунт и связываем провайдер
                    user = createNewUser(email, name, providerId);
                    isNewUser = true;
                    logger.info("New user created. Linking {} provider", providerName);
                    oauthProviderService.linkOAuthProvider(user, providerName, providerId, email);
                }
            }
            
            // Записываем факт входа через этот провайдер
            oauthProviderService.recordLogin(user, providerName);
            
            // Опубликовать событие если это новый пользователь
            if (isNewUser) {
                eventPublisher.publishUserRegistered(user, providerName);
            }
            
            // Генерировать JWT токен
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            logger.info("=== OAuth2 Authentication Successful ===");
            logger.info("User ID: {}", user.getId());
            logger.info("User Email: {}", user.getEmail());
            logger.info("Linked Providers: {}", oauthProviderService.getLinkedProviderNames(user.getId()));
            logger.info("Access Token Generated: {}", accessToken.substring(0, 20) + "...");
            
            // Редирект на фронтенд с токеном
            String redirectUrl = String.format("http://localhost:3000/auth/callback?accessToken=%s&refreshToken=%s&userId=%s", 
                    accessToken, refreshToken, user.getId());
            response.sendRedirect(redirectUrl);
        } else {
            logger.error("Invalid OAuth2User type");
            response.sendRedirect("http://localhost:3000/login?error=invalid_user");
        }
    }

    private Users createNewUser(String email, String name, String uniqueId) {
        logger.info("Creating new OAuth2 user: {}", email);
        
        Users newUser = new Users();
        // НЕ устанавливаем ID - JPA сам сгенерирует через @GeneratedValue(strategy = GenerationType.UUID)
        newUser.setEmail(email);
        String uniqueIdSuffix = uniqueId != null && uniqueId.length() >= 6 ? uniqueId.substring(0, 6) : uniqueId;
        newUser.setUsername(email.split("@")[0] + "_" + uniqueIdSuffix); // Уникальное имя
        newUser.setPassword("OAUTH2_NO_PASSWORD"); // Плейсхолдер - OAuth2 не требует пароля
        newUser.setEmailVerified(true); // OAuth2 провайдер уже проверил email
        newUser.setVerifiedAt(new Date());
        newUser.setAccountState(AccountState.ACTIVE); // Автоматически активируем
        newUser.setCreatedAt(new Date());
        
        userRepository.save(newUser);
        logger.info("New OAuth2 user created: {}", newUser.getId());
        
        return newUser;
    }
    
    /**
     * Извлекает уникальный идентификатор провайдера из атрибутов OAuth2
     * Поддерживает: Google (sub), GitHub (id), и другие стандарты
     */
    private String extractUniqueIdentifier(Map<String, Object> attributes) {
        // Google и OpenID Connect стандарт
        if (attributes.containsKey("sub")) {
            return String.valueOf(attributes.get("sub"));
        }
        // GitHub
        if (attributes.containsKey("id")) {
            return String.valueOf(attributes.get("id"));
        }
        // Fallback: используем login если есть
        if (attributes.containsKey("login")) {
            return String.valueOf(attributes.get("login"));
        }
        // Последний резервный вариант: используем email
        if (attributes.containsKey("email")) {
            return String.valueOf(attributes.get("email")).split("@")[0];
        }
        // Если ничего не найдено, генерируем случайное значение
        return String.valueOf(System.currentTimeMillis());
    }
    
    /**
     * Определяет имя OAuth2 провайдера по URL запроса
     * Примеры: /login/oauth2/code/google → "GOOGLE", /login/oauth2/code/github → "GITHUB"
     */
    private String getAuthProviderName(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        if (requestUri.contains("google")) {
            return "GOOGLE";
        } else if (requestUri.contains("github")) {
            return "GITHUB";
        } else if (requestUri.contains("microsoft")) {
            return "MICROSOFT";
        }
        return "UNKNOWN";
    }
}

