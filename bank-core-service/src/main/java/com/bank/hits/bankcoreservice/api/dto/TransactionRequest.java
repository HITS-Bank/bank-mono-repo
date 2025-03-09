package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransactionRequest {
    private final String accountNumber;
    private final String amount;
}

