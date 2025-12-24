package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

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

//    public Users loginUsers(String email, String password) {
//        if (userRepository.existsByEmail(email)) {
//            throw new IllegalStateException("User not found");
//        }
//
//
//
//    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();

    }
}
