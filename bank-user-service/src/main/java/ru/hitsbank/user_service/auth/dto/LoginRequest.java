package ru.hitsbank.user_service.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
