package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.SignInRequest;
import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    public UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/addUser")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody SignUpRegister request) {
        userService.registerUsers(
                request.username(),
                request.email(),
                request.password()
        );
    }

    @PostMapping("/deleteUser/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUsers(username);
    }

    @PostMapping("/deleteAllUsers")
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }

    @GetMapping("/getAllUsers")
    public List<UserResponse> showUsers() {
        return userService.getAllUsers();
    }



}
