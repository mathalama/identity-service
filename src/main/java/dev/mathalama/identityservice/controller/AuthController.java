package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.SignInRequest;
import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.service.JwtService;
import dev.mathalama.identityservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> signUp(@RequestBody SignUpRegister request) {
        userService.registerUsers(
                request.username(),
                request.email(),
                request.password()
        );
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/signin")
    public ResponseEntity<Map<String, String>> signIn(@RequestBody SignInRequest request) {
        Users user = userService.authenticate(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
