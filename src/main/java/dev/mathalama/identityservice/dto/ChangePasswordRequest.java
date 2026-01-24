package dev.mathalama.identityservice.dto;

public record ChangePasswordRequest(String oldPassword, String newPassword) {
}
