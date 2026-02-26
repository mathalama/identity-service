package dev.mathalama.identityservice.application.service;

import dev.mathalama.identityservice.application.dto.*;
import dev.mathalama.identityservice.domain.entity.Users;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface AuthService {
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    void register(String username, String email, String password);
    Users authenticate(SignInRequest request);
    void changeCurrentPassword(String username, String oldPassword, String newPassword);
    void assignRoleToUser(String username, String roleName);
    VerificationResponse verifyEmail(String token);
    VerificationResponse resendVerificationEmail(String email);
}
