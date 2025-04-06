package com.bank.hits.bankcoreservice.config;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.keycloak.TokenVerifier;
import org.keycloak.common.VerificationException;
import org.keycloak.representations.AccessToken;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    public String extractAccessToken(HttpServletRequest httpServletRequest) {
        try {
            return httpServletRequest.getHeader("Authorization").substring(7);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid token");
        }
    }

    public String getUserId(String token) {
        try {
            return TokenVerifier.create(token, AccessToken.class).getToken().getSubject();
        } catch (VerificationException e) {
            throw new RuntimeException(e);
        }
    }
}