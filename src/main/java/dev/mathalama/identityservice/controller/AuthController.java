package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.ResetPasswordRequest;
import dev.mathalama.identityservice.dto.SignInRequest;
import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    public UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public void signUp(@RequestBody SignUpRegister request) {
        userService.registerUsers(
                request.username(),
                request.email(),
                request.password()
        );
    }

    @PostMapping("/login")
    public void signIn(@RequestBody SignInRequest request) {
        userService.loginUsers(request);
    }

    @PostMapping("/reset")
    public void resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
    }
}
