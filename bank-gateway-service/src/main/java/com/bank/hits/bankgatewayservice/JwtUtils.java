package com.bank.hits.bankgatewayservice;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;

public class JwtUtils {

    private static String secret =  "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1ad1f123a9d2b5f8e3a9c8b5f6v8a3d9";
    private static final String SECRET_KEY = "9a4f2c8d3b7a1e6f45c8a0b3f267d8b1ad1f123a9d2b5f8e3a9c8b5f6a3d9";

    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public static String getClaim(String token, String claimName) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(getSigningKey())// Проверяем подпись
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.get(claimName, String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String Base64Encode(String input) {
        return java.util.Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }
}
