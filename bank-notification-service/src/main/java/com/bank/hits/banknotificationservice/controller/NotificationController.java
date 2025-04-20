package com.bank.hits.banknotificationservice.controller;

import com.bank.hits.banknotificationservice.common.util.JwtUtils;
import com.bank.hits.banknotificationservice.model.RegisterFcmRequest;
import com.bank.hits.banknotificationservice.service.DeviceTokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notification/fcm")
@RequiredArgsConstructor
public class NotificationController {

    private final DeviceTokenService deviceTokenService;
    private final JwtUtils jwtUtils;

    @PostMapping("/register")
    public ResponseEntity<Void> registerToken(
            @Valid @RequestBody RegisterFcmRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String userToken = jwtUtils.extractAccessToken(httpServletRequest);
        deviceTokenService.saveOrUpdateToken(userToken, request.getFcmToken());
        return ResponseEntity.ok().build();
    }
}
