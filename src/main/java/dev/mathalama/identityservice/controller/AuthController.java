package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.SignInRequest;
import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.service.JwtService;
import dev.mathalama.identityservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@RequestBody SignUpRegister request) {
        authService.register(
                request.username(),
                request.email(),
                request.password()
        );
        return Map.of("message", "User registered successfully");
    }

    @PostMapping("/authenticate")
    public ResponseEntity<Map<String, String>> authenticate(@RequestBody SignInRequest request) {
        Users user = authService.authenticate(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
