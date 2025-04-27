package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.config.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankcoreservice.core.service.ClientService;

import java.util.List;
import java.util.UUID;

import static com.bank.hits.bankcoreservice.core.utils.ExceptionUtils.throwExceptionRandomly;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.EMPLOYEES_BASE)
public class EmployeeController {

    private final ClientService clientService;

    private final JwtUtils jwtUtils;

    @PostMapping(value = ApiConstants.BLOCK_CLIENT_ACCOUNTS , produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> blockClientAccounts(@PathVariable("clientId") final UUID clientId) {
        throwExceptionRandomly();
        clientService.blockClientAccounts(clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = ApiConstants.UNBLOCK_CLIENT_ACCOUNTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> unblockClientAccounts(@PathVariable("clientId") final UUID clientId) {
        throwExceptionRandomly();
        clientService.unblockClientAccounts(clientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = ApiConstants.CLIENT_INFO, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountDto>> getAccountsList(
            final HttpServletRequest httpServletRequest,
            @PathVariable("userId") final UUID clientId,
            @RequestParam final int pageSize,
            @RequestParam final int pageNumber
    ) {
        throwExceptionRandomly();
        final UUID employeeId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(clientService.getAccountsList(clientId, employeeId, pageSize, pageNumber - 1));
    }

}
