package com.bank.hits.bankuserservice.auth.dto;

public record TokenResponse(
        String accessToken,
        String accessTokenExpiresAt,
        String refreshToken,
        String refreshTokenExpiresAt
) {
}
