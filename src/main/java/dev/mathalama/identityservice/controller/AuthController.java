package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.ResetPasswordRequest;
import dev.mathalama.identityservice.dto.SessionInfo;
import dev.mathalama.identityservice.dto.SignInRequest;
import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.entity.Users;
import dev.mathalama.identityservice.service.SessionService;
import dev.mathalama.identityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final SessionService sessionService;

    public AuthController(UserService userService, SessionService sessionService) {
        this.userService = userService;
        this.sessionService = sessionService;
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

    @PostMapping("/login")
    public ResponseEntity<SessionInfo> signIn(
            @RequestBody SignInRequest request,
            HttpServletRequest httpRequest
    ) {
        Users user = userService.authenticate(request);
        sessionService.createSession(httpRequest, user);
        SessionInfo sessionInfo = sessionService.getCurrentSessionInfo(httpRequest);
        return ResponseEntity.ok(sessionInfo);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request) {
        if (!sessionService.hasActiveSession(request)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session");
        }
        sessionService.invalidateSession(request);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/session")
    public ResponseEntity<SessionInfo> getCurrentSession(HttpServletRequest request) {
        SessionInfo sessionInfo = sessionService.getCurrentSessionInfo(request);
        if (sessionInfo == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session");
        }
        return ResponseEntity.ok(sessionInfo);
    }

    @GetMapping("/session/check")
    public ResponseEntity<Map<String, Boolean>> checkSession(HttpServletRequest request) {
        boolean hasSession = sessionService.hasActiveSession(request);
        return ResponseEntity.ok(Map.of("authenticated", hasSession));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionInfo>> getUserSessions(HttpServletRequest request) {
        String username = sessionService.getCurrentUsername(request);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session");
        }
        List<SessionInfo> sessions = sessionService.getUserSessions(username);
        return ResponseEntity.ok(sessions);
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Map<String, String>> invalidateAllSessions(HttpServletRequest request) {
        String username = sessionService.getCurrentUsername(request);
        if (username == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session");
        }
        sessionService.invalidateAllUserSessions(username);
        return ResponseEntity.ok(Map.of("message", "All sessions invalidated"));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Map<String, String>> invalidateSession(
            @PathVariable String sessionId,
            HttpServletRequest request
    ) {
        if (!sessionService.hasActiveSession(request)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No active session");
        }
        sessionService.invalidateSessionById(sessionId);
        return ResponseEntity.ok(Map.of("message", "Session invalidated"));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
