package com.bank.hits.bankuserservice.profile.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.model.UserEntity;
import com.bank.hits.bankuserservice.common.util.JwtUtils;
import com.bank.hits.bankuserservice.profile.dto.UserListRequest;
import com.bank.hits.bankuserservice.profile.service.ProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/employee/profile")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> ban(
            @PathVariable UUID userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        UUID employeeId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        profileService.banUser(employeeId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unban(
            @PathVariable UUID userId,
            HttpServletRequest httpServletRequest
    ) throws JsonProcessingException {
        UUID employeeId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        profileService.unbanUser(employeeId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserDto>> getUserList(
            @RequestParam UserEntity.Role role,
            @RequestParam(required = false) String nameQuery,
            @RequestParam int pageSize,
            @RequestParam int pageNumber,
            HttpServletRequest httpServletRequest
    ) {
        UUID employeeId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        UserListRequest request = new UserListRequest(role, nameQuery, pageSize, pageNumber);
        return ResponseEntity.ok(profileService.getUserList(employeeId, request));
    }
}
