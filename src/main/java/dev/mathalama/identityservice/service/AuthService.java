package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.*;
import dev.mathalama.identityservice.dto.enums.AccountState;
import dev.mathalama.identityservice.dto.enums.SecurityStatus;
import dev.mathalama.identityservice.entity.*;
import dev.mathalama.identityservice.exception.UnauthorizedException;
import dev.mathalama.identityservice.exception.UserAlreadyExistException;
import dev.mathalama.identityservice.exception.UserNotFoundException;
import dev.mathalama.identityservice.repository.RoleRepository;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

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
            user.setAccountState(AccountState.ACTIVE);
            user.setSecurityStatus(SecurityStatus.MFA_REQUIRED);

            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistException("Username or email already exists");
        }
    }

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
    
    public void assignRoleToUser(String username, String roleName) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new UserNotFoundException("Role not found"));
        
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Assigned role {} to user {}", roleName, username);
    }

}
