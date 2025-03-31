package com.bank.hits.bankuserservice.controller;

import com.bank.hits.bankuserservice.model.dto.UserDto;
import com.bank.hits.bankuserservice.common.util.JwtUtils;
import com.bank.hits.bankuserservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/profile") // TODO убрать слово profile
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping
    public ResponseEntity<UserDto> getSelfProfile(
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(userService.getSelfUserProfile(token));
    }
}
