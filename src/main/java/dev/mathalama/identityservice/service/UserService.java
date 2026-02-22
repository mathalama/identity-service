package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.dto.enums.AccountState;
import dev.mathalama.identityservice.dto.enums.SecurityStatus;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.exception.UserNotFoundException;
import dev.mathalama.identityservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    // Retrieves a user by UUID and returns its DTO representation.
    // Throws UserNotFoundException if the user does not exist.
    @Transactional(readOnly = true)
    public UserResponse getById(UUID id) {
        return mapToResponse(findUserById(id));
    }

    // Retrieves a user by username and returns its DTO representation.
    // Throws UserNotFoundException if no matching user is found.
    @Transactional(readOnly = true)
    public UserResponse getByUsername(String username) {
        return mapToResponse(findUserByUsername(username));
    }

    // Returns the currently authenticated user as a DTO.
    // Requires Authentication principal to be of type Users.
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        return mapToResponse(getCurrentAuthenticatedUser());
    }

    // Soft-deletes the currently authenticated user by updating account state
    // and security status. Does not physically remove the record from the database.
    @Transactional
    public UserResponse deleteCurrentUser() {
        Users user = getCurrentAuthenticatedUser();

        user.setAccountState(AccountState.DELETED);
        user.setSecurityStatus(SecurityStatus.LOCKED);

        return mapToResponse(user);
    }

    // Extracts the authenticated Users entity from the SecurityContext.
    // Now handles both Users principal (from JWT filter) and String principal (fallback).
    private Users getCurrentAuthenticatedUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UserNotFoundException("No authenticated user found");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Users user) {
            return user;
        }

        // Fallback: если principal — строка (userId), загружаем из БД
        if (principal instanceof String userId) {
            return userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new UserNotFoundException("User not found"));
        }

        throw new UserNotFoundException("Unknown principal type");
    }
    // Finds a user by UUID or throws UserNotFoundException.
    private Users findUserById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // Finds a user by username or throws UserNotFoundException.
    private Users findUserByUsername(String username){
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    // Maps a Users entity to its corresponding UserResponse DTO.
    private UserResponse mapToResponse(Users user) {
        return UserResponse.from(user);
    }
}
