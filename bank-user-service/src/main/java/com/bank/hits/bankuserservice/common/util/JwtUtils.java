package com.bank.hits.bankuserservice.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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

    private Claims extractAllClaims(String token, String signingKey) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}