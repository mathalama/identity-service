package dev.mathalama.identityservice.service;

import dev.mathalama.identityservice.entity.Role;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.repository.RoleRepository;
import dev.mathalama.identityservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role();
        userRole.setName("ROLE_USER");
    }

    @Test
    void registerUsers_shouldSaveUser_whenRoleExists() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";

        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        userService.registerUsers(username, email, password);

        verify(userRepository, times(1)).save(any(Users.class));
    }

    @Test
    void assignRoleToUser_shouldAddRole_whenUserAndRoleExist() {
        String username = "testuser";
        String roleName = "ROLE_ADMIN";
        Users user = Users.builder()
                .username(username)
                .email("test@example.com")
                .password("password")
                .roles(new java.util.HashSet<>(Set.of(userRole)))
                .build();
        Role adminRole = new Role();
        adminRole.setName(roleName);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(roleRepository.findByName(roleName)).thenReturn(Optional.of(adminRole));

        userService.assignRoleToUser(username, roleName);

        assertTrue(user.getRoles().contains(adminRole));
        verify(userRepository, times(1)).save(user);
    }
}
