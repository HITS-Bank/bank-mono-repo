package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientDto;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientInfoDto;
import ru.ciklon.bank.bankcoreservice.core.service.ClientService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.CLIENTS_BASE)
public class ClientController {

    private final ClientService clientService;

    @GetMapping(ApiConstants.CLIENT_INFO)
    public ResponseEntity<ClientInfoDto> getClientInfo(@PathVariable("clientId") final UUID clientId, @RequestParam final UUID employeeId) {
        return ResponseEntity.ok(clientService.getClientInfo(clientId, employeeId));
    }

    // create client
    @PostMapping(ApiConstants.CREATE_CLIENT)
    public ResponseEntity<ClientDto> createClient(final ClientDto clientDto) {
        return ResponseEntity.ok(clientService.createClient(clientDto));
    }
}
