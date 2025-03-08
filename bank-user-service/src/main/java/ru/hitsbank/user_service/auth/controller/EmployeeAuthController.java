package ru.hitsbank.user_service.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hitsbank.user_service.auth.dto.RegisterRequest;
import ru.hitsbank.user_service.auth.service.AuthService;
import ru.hitsbank.user_service.common.util.JwtUtils;

import java.util.UUID;

@RestController
@RequestMapping("/employee/auth")
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
