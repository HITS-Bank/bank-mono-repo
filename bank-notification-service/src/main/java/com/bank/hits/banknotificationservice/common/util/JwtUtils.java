package com.bank.hits.banknotificationservice.common.util;

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
}