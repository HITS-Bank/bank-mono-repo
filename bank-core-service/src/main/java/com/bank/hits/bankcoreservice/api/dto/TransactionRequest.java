package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransactionRequest {
    private final UUID accountId;
    private final String amount;
}
