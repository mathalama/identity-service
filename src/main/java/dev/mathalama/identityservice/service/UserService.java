package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.info("Adding new user!");
        if (userRepository.existsByEmail(email)) {
            log.warn("Email already exists");
            throw new IllegalStateException("Email already exists");
        }
        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists");
            throw new IllegalStateException("Username already exists");
        }
        String encodePassword = passwordEncoder.encode(password);

        Users user = Users.builder()
                .username(username)
                .email(email)
                .password(encodePassword)
                .build();

        userRepository.save(user);
        log.info("User was created");
    }

//    public Users loginUsers(String email, String password) {
//        if (userRepository.existsByEmail(email)) {
//            throw new IllegalStateException("User not found");
//        }
//
//
//
//    }

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
}
