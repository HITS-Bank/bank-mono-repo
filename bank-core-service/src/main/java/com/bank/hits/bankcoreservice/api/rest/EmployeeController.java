package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankcoreservice.core.service.ClientService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.EMPLOYEES_BASE)
public class EmployeeController {

    private final ClientService clientService;

    @PostMapping(ApiConstants.BLOCK_CLIENT_ACCOUNTS)
    public ResponseEntity<Void> blockClientAccounts(@PathVariable("clientId") final UUID clientId) {
        clientService.blockClientAccounts(clientId);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ApiConstants.UNBLOCK_CLIENT_ACCOUNTS)
    public ResponseEntity<Void> unblockClientAccounts(@PathVariable("clientId") final UUID clientId) {
        clientService.unblockClientAccounts(clientId);
        return ResponseEntity.ok().build();
    }
}
