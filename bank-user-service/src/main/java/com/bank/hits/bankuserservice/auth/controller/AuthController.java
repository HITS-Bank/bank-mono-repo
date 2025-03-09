package com.bank.hits.bankuserservice.auth.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankuserservice.auth.dto.LoginRequest;
import com.bank.hits.bankuserservice.auth.dto.RefreshTokenRequest;
import com.bank.hits.bankuserservice.auth.dto.TokenResponse;
import com.bank.hits.bankuserservice.auth.service.AuthService;
import com.bank.hits.bankuserservice.common.model.Channel;

@RestController
@RequestMapping("/users/auth")
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
