package com.bank.hits.bankuserservice.profile.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.util.JwtUtils;
import com.bank.hits.bankuserservice.profile.service.ProfileService;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final JwtUtils jwtUtils;

    @GetMapping("")
    public ResponseEntity<UserDto> getSelfProfile(
            HttpServletRequest httpServletRequest
    ) {
        UUID userId = jwtUtils.extractUserIdFromRequest(httpServletRequest);
        return ResponseEntity.ok(profileService.getSelfProfile(userId));
    }
}
