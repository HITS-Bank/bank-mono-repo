package com.bank.hits.bankuserservice.controller;

import com.bank.hits.bankuserservice.model.dto.UserDto;
import com.bank.hits.bankuserservice.common.util.JwtUtils;
import com.bank.hits.bankuserservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.bank.hits.bankuserservice.common.util.ExceptionUtils.throwExceptionRandomly;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getSelfProfile(
            HttpServletRequest httpServletRequest
    ) {
        throwExceptionRandomly();
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(userService.getSelfUserProfile(token));
    }
}
