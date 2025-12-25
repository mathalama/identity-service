package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.CreateUserRequest;
import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.entity.Users;
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

    @GetMapping("/info")
    public String getInfo() {
        return "Hello, actually im good.";
    }

    @PostMapping("/addUser")
    @ResponseStatus(HttpStatus.CREATED)
    public void createUser(@RequestBody CreateUserRequest request) {
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

    @PostMapping("/deleteUsers")
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }

    @GetMapping("/getAll")
    public List<UserResponse> showUsers() {
        return userService.getAllUsers();
    }


}
