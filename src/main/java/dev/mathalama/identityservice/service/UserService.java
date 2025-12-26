package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.*;
import dev.mathalama.identityservice.entity.*;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public void registerUsers(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            throw new IllegalStateException("Username already exists");
        }
        String encodePassword = passwordEncoder.encode(password);

        Users user = Users.builder()
                .username(username)
                .email(email)
                .password(encodePassword)
                .build();

        userRepository.save(user);
    }

    public void loginUsers(SignInRequest request) {
        Users user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Invalid email or password"));


        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalStateException("Invalid email or password");
        }
    }

    public List<UserResponse> getAllUsers() {
        log.info("Users will printed");
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();

    }

    public void deleteUsers(String username) {
        log.info("Deleting user");
        if (userRepository.existsByUsername(username)) {
            userRepository.deleteByUsername(username);
        } else {
            log.warn("Users not found");
            throw new IllegalStateException("User not found");
        }
    }

    public void deleteAllUsers() {
        log.info("Deleting all users");
        if (userRepository.count() > 0) {
            userRepository.deleteAll();
            log.info("All users was deleted");
        } else {
            log.warn("The database is empty");
        }
    }

    public void resetPassword(ResetPasswordRequest request) {
        Users user = userRepository
                .findByEmailIgnoreCaseOrUsernameIgnoreCase(
                        request.login(), request.login()
                )
                .orElseThrow(() ->
                        new IllegalStateException("Invalid credentials")
                );
        user.setPassword(passwordEncoder.encode(request.password()));
    }
}
