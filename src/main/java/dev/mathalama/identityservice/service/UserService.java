package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.*;
import dev.mathalama.identityservice.entity.*;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void registerUsers(String username, String email, String password) {
        String encodePassword = passwordEncoder.encode(password);
        try {
            Users user = Users.builder()
                    .username(username)
                    .email(email)
                    .password(encodePassword)
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
        // TODO: issue JWT token
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


    public void resetPassword(ResetPasswordRequest request) {
        Users user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        request.login(), request.login()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(UNAUTHORIZED, "Invalid credentials")
                );
        user.setPassword(passwordEncoder.encode(request.password()));
    }
}
