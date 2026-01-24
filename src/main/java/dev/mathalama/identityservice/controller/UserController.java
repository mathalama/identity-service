package dev.mathalama.identityservice.controller;

import dev.mathalama.identityservice.dto.SignUpRegister;
import dev.mathalama.identityservice.dto.UserResponse;
import dev.mathalama.identityservice.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @DeleteMapping("/{username}")
    public void deleteUser(@PathVariable String username) {
        userService.deleteUsers(username);
    }

    @DeleteMapping("/all")
    public void deleteAllUsers() {
        userService.deleteAllUsers();
    }

    @GetMapping("/all")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
    
    @PostMapping("/{username}/role")
    public Map<String, String> assignRole(@PathVariable String username, @RequestParam String role) {
        userService.assignRoleToUser(username, role);
        return Map.of("message", "Role assigned successfully");
    }
}
