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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/employee/profile") // TODO убрать слово profile
@RequiredArgsConstructor
public class EmployeeUserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<UserDto> getSelfProfile(
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(userService.getSelfUserProfile(token));
    }

    @PostMapping("/register")
//    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.registerUser(token, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/ban")
//    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> banUser(
            @PathVariable("userId") String userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.banUser(token, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/unban")
//    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<Void> unbanUser(
            @PathVariable("userId") String userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        userService.unbanUser(token, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/list")
//    @PreAuthorize("hasRole('EMPLOYEE')")
    public ResponseEntity<List<UserDto>> getUserList(
            @RequestParam Role role,
            @RequestParam(required = false) String nameQuery,
            @RequestParam int pageSize,
            @RequestParam @Positive int pageNumber,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        UserListRequest request = new UserListRequest(role, nameQuery, pageSize, pageNumber);
        return ResponseEntity.ok(userService.getUserList(token, request));
    }
}
