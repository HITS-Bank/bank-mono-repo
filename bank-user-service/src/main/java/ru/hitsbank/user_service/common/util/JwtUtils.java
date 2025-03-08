package ru.hitsbank.user_service.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.hitsbank.user_service.common.dto.UserDto;

import java.security.Key;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_ROLE = "role";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.accessToken.expirationMillis}")
    private Long accessTokenExpirationMillis;

    @Value("${jwt.refreshToken.expirationMillis}")
    private Long refreshTokenExpirationMillis;

    public UUID extractUserIdFromRequest(HttpServletRequest httpServletRequest) {
        String token = httpServletRequest.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            try {
                return extractUserId(token);
            } catch (JwtException e) {
                throw new IllegalArgumentException("Invalid token", e);
            }
        } else {
            throw new IllegalArgumentException("Authorization header is missing or invalid");
        }
    }

    public String generateAccessToken(UserDto user) {
        return generateToken(user, accessTokenExpirationMillis);
    }

    public String generateRefreshToken(UserDto user) {
        return generateToken(user, refreshTokenExpirationMillis);
    }

    public UUID extractUserId(String token) {
        String userId = extractAllClaims(token).get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userId);
    }

    public Date extractExpirationMillis(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsProducer) {
        Claims claims = extractAllClaims(token);
        return claimsProducer.apply(claims);
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String generateToken(UserDto user, Long expirationMillis) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim(CLAIM_USER_ID, user.getId().toString())
                .claim(CLAIM_USER_ROLE, user.getRole().name())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractExpirationMillis(token).before(new Date());
    }
}