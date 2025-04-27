package com.bank.hits.bankuserservice.controller;

import com.bank.hits.bankuserservice.common.util.IdempotencyUtils;
import com.bank.hits.bankuserservice.model.dto.RegisterRequest;
import com.bank.hits.bankuserservice.model.dto.UserDto;
import com.bank.hits.bankuserservice.common.enums.Role;
import com.bank.hits.bankuserservice.common.util.JwtUtils;
import com.bank.hits.bankuserservice.model.dto.UserListRequest;
import com.bank.hits.bankuserservice.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.bank.hits.bankuserservice.common.util.ExceptionUtils.throwExceptionRandomly;

@RestController
@RequestMapping("/users/employee/users")
@RequiredArgsConstructor
public class EmployeeUserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final IdempotencyUtils idempotency;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        return idempotency.handleIdempotency(request.getRequestId(), () -> {
            throwExceptionRandomly();
            String token = jwtUtils.extractAccessToken(httpServletRequest);
            userService.registerUser(token, request);
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable("userId") String userId,
            @RequestParam("requestId") UUID requestId,
            HttpServletRequest httpServletRequest
    ) {
        return idempotency.handleIdempotency(requestId, () -> {
            throwExceptionRandomly();
            String token = jwtUtils.extractAccessToken(httpServletRequest);

            try {
                userService.banUser(token, userId);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.noContent().build();
        });
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @PathVariable("userId") String userId,
            @RequestParam("requestId") UUID requestId,
            HttpServletRequest httpServletRequest
    ) {
        return idempotency.handleIdempotency(requestId, () -> {
            throwExceptionRandomly();
            String token = jwtUtils.extractAccessToken(httpServletRequest);

            try {
                userService.unbanUser(token, userId);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return ResponseEntity.noContent().build();
        });
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getUserList(
            @RequestParam Role role,
            @RequestParam(required = false) String nameQuery,
            @RequestParam int pageSize,
            @RequestParam @Positive int pageNumber,
            HttpServletRequest httpServletRequest
    ) {
        throwExceptionRandomly();
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        UserListRequest request = new UserListRequest(role, nameQuery, pageSize, pageNumber);
        return ResponseEntity.ok(userService.getUserList(token, request));
    }
}
