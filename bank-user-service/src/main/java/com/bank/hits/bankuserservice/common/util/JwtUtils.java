package com.bank.hits.bankuserservice.common.util;

import com.bank.hits.bankuserservice.service.KeycloakClientService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtils {

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_USER_ROLE = "role";

    private final KeycloakClientService keycloakClientService;

//    public UUID extractUserIdFromRequest(HttpServletRequest httpServletRequest) {
//        String token = httpServletRequest.getHeader("Authorization");
//
//        if (token != null && token.startsWith("Bearer ")) {
//            token = token.substring(7);
//            try {
//                return extractUserId(token);
//            } catch (JwtException e) {
//                throw new IllegalArgumentException("Invalid token", e);
//            }
//        } else {
//            throw new IllegalArgumentException("Authorization header is missing or invalid");
//        }
//    }

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

//    public String generateAccessToken(UserDto user) {
//        return generateToken(user, accessTokenExpirationMillis);
//    }
//
//    public String generateRefreshToken(UserDto user) {
//        return generateToken(user, refreshTokenExpirationMillis);
//    }
//
//    public UUID extractUserId(String token) {
//        String userId = extractAllClaims(token).get(CLAIM_USER_ID, String.class);
//        return UUID.fromString(userId);
//    }
//
//    public Date extractExpirationMillis(String token) {
//        return extractClaim(token, Claims::getExpiration);
//    }
//
//    public <T> T extractClaim(String token, Function<Claims, T> claimsProducer) {
//        Claims claims = extractAllClaims(token);
//        return claimsProducer.apply(claims);
//    }
//
//    public boolean isTokenValid(String token) {
//        try {
//            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
//            return !isTokenExpired(token);
//        } catch (JwtException | IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    private String generateToken(UserDto user, Long expirationMillis) {
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .claim(CLAIM_USER_ID, user.getId().toString())
//                .claim(CLAIM_USER_ROLE, user.getRole().name())
//                .setIssuedAt(new Date(System.currentTimeMillis()))
//                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
//                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
//                .compact();
//    }
//
//    private Key getSigningKey() {
//        return Keys.hmacShaKeyFor(secret.getBytes());
//    }
//
//
//    private boolean isTokenExpired(String token) {
//        return extractExpirationMillis(token).before(new Date());
//    }
}