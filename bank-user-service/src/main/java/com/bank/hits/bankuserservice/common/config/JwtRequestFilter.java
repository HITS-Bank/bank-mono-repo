package com.bank.hits.bankuserservice.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.service.UserService;
import com.bank.hits.bankuserservice.common.util.JwtUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ROLE_PREFIX = "ROLE_";
    private static final int TOKEN_START_INDEX = 7;

    private final JwtUtils jwtUtils;
    private final UserService userService;

    private static final List<String> noTokenAuthPaths = List.of(
            "/auth/login"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String requestPath = request.getRequestURI();

        boolean isNoTokenAuthPath = noTokenAuthPaths.stream().anyMatch(requestPath::startsWith);
        if (isNoTokenAuthPath) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        String jwt = null;

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            jwt = authHeader.substring(TOKEN_START_INDEX);
        }

        if (jwt != null) {
            try {
                UUID userId = jwtUtils.extractUserId(jwt);
                if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDto user = null;
                    try {
                        user = userService.getUserById(userId);
                    } catch (Exception e) {
                        log.error("Error fetching user with ID: {}", userId, e);
                    }

                    if (user != null) {
                        List<GrantedAuthority> authorities = Collections.singletonList(
                                new SimpleGrantedAuthority(ROLE_PREFIX + user.getRole().name())
                        );

                        UsernamePasswordAuthenticationToken authenticationToken =
                                new UsernamePasswordAuthenticationToken(user, jwt, authorities);

                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            } catch (Exception e) {
                log.error("Error processing JWT", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
