package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransferAccountInfo {
    private UUID accountId;
    private String accountNumber;
    private CurrencyCode accountCurrencyCode;
}
