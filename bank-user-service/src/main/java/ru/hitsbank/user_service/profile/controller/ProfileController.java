package ru.hitsbank.user_service.profile.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hitsbank.user_service.common.dto.UserDto;
import ru.hitsbank.user_service.profile.dto.UserListRequest;
import ru.hitsbank.user_service.common.model.UserEntity;
import ru.hitsbank.user_service.common.util.JwtUtils;
import ru.hitsbank.user_service.profile.service.ProfileService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/employee/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @PostMapping("/{userId}/ban")
    public ResponseEntity<Void> ban(
            @PathVariable UUID userId,
            HttpServletRequest httpServletRequest
    ) {
        UUID employeeId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        profileService.banUser(employeeId, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/unban")
    public ResponseEntity<Void> unban(
            @PathVariable UUID userId,
            HttpServletRequest httpServletRequest
    ) {
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
