package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.*;
import dev.mathalama.identityservice.entity.*;
import dev.mathalama.identityservice.repository.RoleRepository;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public void registerUsers(String username, String email, String password) {
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

            userRepository.save(user);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    CONFLICT, "Username or email already exists"
            );
        }
    }

    public Users authenticate(SignInRequest request) {
        Users user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        request.login(), request.login()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                UNAUTHORIZED,
                                "Invalid email or password"
                        )
                );

        if (!passwordEncoder.matches(
                request.password(),
                user.getPassword()
        )) {
            throw new ResponseStatusException(
                    UNAUTHORIZED,
                    "Invalid credentials"
            );
        }

        return user;
    }

    ///  Get all users
    public List<UserResponse> getAllUsers() {
        log.info("Fetching all users");

        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }

    /// delete which user
    public void deleteUsers(String username) {
        log.info("Deleting user");
        if (userRepository.existsByUsername(username)) {
            userRepository.deleteByUsername(username);
        } else {
            log.warn("Users not found");
            throw new ResponseStatusException(NOT_FOUND,"User not found");
        }
    }

    /// delete all users
    public void deleteAllUsers() {
        log.info("Deleting all users");
        userRepository.deleteAll();
        log.info("All users have been deleted");
    }

    /// Get total user count
    public long userCount() {
        return userRepository.count();
    }


    public void changePassword(String username, String oldPassword, String newPassword) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid old password");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed successfully for user: {}", username);
    }
    
    public void assignRoleToUser(String username, String roleName) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "User not found"));
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Role not found"));
        
        user.getRoles().add(role);
        userRepository.save(user);
        log.info("Assigned role {} to user {}", roleName, username);
    }
}
