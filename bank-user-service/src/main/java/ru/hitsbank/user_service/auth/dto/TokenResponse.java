package ru.hitsbank.user_service.auth.dto;

public record TokenResponse(
        String accessToken,
        String accessTokenExpiresAt,
        String refreshToken,
        String refreshTokenExpiresAt
) {
}
