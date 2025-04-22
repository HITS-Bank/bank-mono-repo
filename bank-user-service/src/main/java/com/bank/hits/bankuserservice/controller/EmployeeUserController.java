package com.bank.hits.bankuserservice.controller;

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

import static com.bank.hits.bankuserservice.common.util.ExceptionUtils.throwExceptionRandomly;

@RestController
@RequestMapping("/users/employee/users")
@RequiredArgsConstructor
public class EmployeeUserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        throwExceptionRandomly();
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.registerUser(token, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> banUser(
            @PathVariable("userId") String userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        throwExceptionRandomly();
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.banUser(token, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unbanUser(
            @PathVariable("userId") String userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        throwExceptionRandomly();
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.unbanUser(token, userId);
        return ResponseEntity.noContent().build();
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
