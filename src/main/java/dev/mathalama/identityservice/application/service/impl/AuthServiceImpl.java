package dev.mathalama.identityservice.application.service.impl;

import dev.mathalama.identityservice.application.dto.*;
import dev.mathalama.identityservice.application.service.AuthService;
import dev.mathalama.identityservice.application.service.EmailService;
import dev.mathalama.identityservice.application.service.VerificationTokenService;
import dev.mathalama.identityservice.domain.entity.Role;
import dev.mathalama.identityservice.domain.entity.Users;
import dev.mathalama.identityservice.domain.enums.AccountState;
import dev.mathalama.identityservice.domain.enums.SecurityStatus;
import dev.mathalama.identityservice.domain.exception.UnauthorizedException;
import dev.mathalama.identityservice.domain.exception.UserAlreadyExistException;
import dev.mathalama.identityservice.domain.exception.UserNotFoundException;
import dev.mathalama.identityservice.infrastructure.repository.RoleRepository;
import dev.mathalama.identityservice.infrastructure.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public void register(String username, String email, String password) {
        String encodePassword = passwordEncoder.encode(password);
        
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Default role not found"));
        
        try {
            Users user = Users.builder()
                    .username(username)
                    .email(email)
                    .password(encodePassword)
                    .roles(Set.of(userRole))
                    .build();
            user.setAccountState(AccountState.PENDING_VERIFICATION);
            user.setSecurityStatus(SecurityStatus.MFA_REQUIRED);
            user.setEmailVerified(false);
            user.setCreatedAt(new Date());

            Users savedUser = userRepository.save(user);
            
            // Generate and send verification token
            String verificationToken = verificationTokenService.generateVerificationToken(savedUser);
            emailService.sendVerificationEmail(email, username, verificationToken);
            
            log.info("User registered successfully: {} (email: {}). Verification email sent.", username, email);
            
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistException("Username or email already exists");
        }
    }

    @Override
    public Users authenticate(SignInRequest request) {
        Users user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        request.login(), request.login()
                )
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid email or password")
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new UnauthorizedException("Invalid credentials");
        }

        return user;
    }

    @Override
    public void changeCurrentPassword(String username, String oldPassword, String newPassword) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }
    
    @Override
    public void assignRoleToUser(String username, String roleName) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new UserNotFoundException("Role not found"));
        
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Assigned role {} to user {}", roleName, username);
    }

    @Override
    public VerificationResponse verifyEmail(String token) {
        if (!verificationTokenService.verifyToken(token)) {
            throw new UnauthorizedException("Invalid or expired verification token");
        }

        // Get userId from Redis token
        String userIdStr = verificationTokenService.getUserIdByToken(token);
        
        if (userIdStr == null) {
            throw new UserNotFoundException("Token not found or expired");
        }

        // Find user by UUID
        UUID userId = UUID.fromString(userIdStr);
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        // Update user account
        user.setEmailVerified(true);
        user.setVerifiedAt(new Date());
        user.setAccountState(AccountState.ACTIVE);
        userRepository.save(user);

        // Mark token as used (delete from Redis)
        verificationTokenService.markTokenAsUsed(token);

        log.info("Email verified successfully for user: {}", user.getUsername());

        return new VerificationResponse("Email verified successfully", true);
    }

    @Override
    public VerificationResponse resendVerificationEmail(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAccountState() != AccountState.PENDING_VERIFICATION) {
            throw new ResponseStatusException(BAD_REQUEST, "User email is already verified or account is not pending");
        }

        if (!verificationTokenService.canResendToken(user)) {
            throw new ResponseStatusException(BAD_REQUEST, "Verification email was recently sent. Please wait before requesting another.");
        }

        // Generate new token
        String verificationToken = verificationTokenService.generateVerificationToken(user);
        user.setLastVerificationSentAt(new Date());
        userRepository.save(user);

        // Send email
        emailService.sendVerificationEmail(email, user.getUsername(), verificationToken);

        log.info("Verification email resent to user: {}", user.getUsername());

        return new VerificationResponse("Verification email sent successfully", false);
    }
}
