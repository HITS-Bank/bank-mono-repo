package ru.hitsbank.user_service.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hitsbank.user_service.common.model.Channel;
import ru.hitsbank.user_service.auth.dto.LoginRequest;
import ru.hitsbank.user_service.auth.dto.RefreshTokenRequest;
import ru.hitsbank.user_service.auth.dto.TokenResponse;
import ru.hitsbank.user_service.auth.service.AuthService;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody LoginRequest request,
            @RequestParam("channel") Channel channel
    ) {
        return ResponseEntity.ok(authService.login(request, channel));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(request));
    }
}
