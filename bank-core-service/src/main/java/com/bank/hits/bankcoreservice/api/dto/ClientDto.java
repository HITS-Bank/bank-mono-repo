package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@Data
@RequiredArgsConstructor
public class ClientDto {
    private UUID clientId;
}
