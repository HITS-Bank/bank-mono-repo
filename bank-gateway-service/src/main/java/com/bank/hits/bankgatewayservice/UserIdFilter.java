package com.bank.hits.bankgatewayservice;


import io.jsonwebtoken.security.Keys;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.Optional;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class UserIdFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpHeaders headers = request.getHeaders();

        Optional<String> userId = extractUserId(headers.getFirst(HttpHeaders.AUTHORIZATION));
        Optional<String> role = extractUserRole(headers.getFirst(HttpHeaders.AUTHORIZATION));
        if (userId.isPresent() && role.isPresent()) {
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("userId", userId.get())
                    .header("role", role.get())
                    .build();
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }
        return chain.filter(exchange);
    }

    private Optional<String> extractUserId(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String userId = JwtUtils.getClaim(token, "userId");
            return Optional.ofNullable(userId);
        }
        return Optional.empty();
    }

    private Optional<String> extractUserRole(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            String role = JwtUtils.getClaim(token, "role");
            return Optional.ofNullable(role);
        }
        return Optional.empty();
    }

    private SecretKey getSigningKey() throws DecoderException {
        byte[] keyBytes = Hex.decodeHex("9a4f2c8d3b7a1e6f45c8a0b3f267d8b1ad1f123a9d2b5f8e3a9c8b5f6a3d9");
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
