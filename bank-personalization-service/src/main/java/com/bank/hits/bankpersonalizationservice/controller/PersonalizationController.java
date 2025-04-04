package com.bank.hits.bankpersonalizationservice.controller;

import com.bank.hits.bankpersonalizationservice.PersonalizationService;
import com.bank.hits.bankpersonalizationservice.common.enums.Role;
import com.bank.hits.bankpersonalizationservice.model.dto.HiddenAccountsDto;
import com.bank.hits.bankpersonalizationservice.model.dto.ThemeDto;
import com.bank.hits.bankpersonalizationservice.common.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/personalization")
@RequiredArgsConstructor
public class PersonalizationController {

    private final JwtUtils jwtUtils;

    private final PersonalizationService personalizationService;

    @PostMapping("/theme")
    public ResponseEntity<ThemeDto> setTheme(
            @RequestParam("channel") Role channel,
            @RequestBody ThemeDto themeDto,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(personalizationService.setTheme(token, channel, themeDto));
    }

    @GetMapping("/theme")
    public ResponseEntity<ThemeDto> getTheme(
            @RequestParam("channel") Role channel,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(personalizationService.getTheme(token, channel));
    }

    @PostMapping("/hiddenAccount")
    public ResponseEntity<Void> addHiddenAccount(
            @RequestParam("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        personalizationService.addHiddenAccount(token, accountId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/hiddenAccount")
    public ResponseEntity<Void> deleteHiddenAccount(
            @RequestParam("accountId") UUID accountId,
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        personalizationService.deleteHiddenAccount(token, accountId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hiddenAccount/list")
    public ResponseEntity<HiddenAccountsDto> getHiddenAccountList(
            HttpServletRequest httpServletRequest
    ) {
        String token = jwtUtils.extractAccessToken(httpServletRequest);
        return ResponseEntity.ok(personalizationService.getHiddenAccountList(token));
    }
}
