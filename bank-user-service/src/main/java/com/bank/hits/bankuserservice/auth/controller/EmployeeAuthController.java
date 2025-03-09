package com.bank.hits.bankuserservice.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankuserservice.auth.dto.RegisterRequest;
import com.bank.hits.bankuserservice.auth.service.AuthService;
import com.bank.hits.bankuserservice.common.util.JwtUtils;

import java.util.UUID;

@RestController
@RequestMapping("/users/employee/auth")
@RequiredArgsConstructor
public class EmployeeAuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        UUID employeeId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        authService.register(request, employeeId);
        return ResponseEntity.ok().build();
    }
}
