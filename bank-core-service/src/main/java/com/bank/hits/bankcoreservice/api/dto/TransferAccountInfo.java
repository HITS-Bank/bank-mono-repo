package com.bank.hits.bankcoreservice.api.dto;

import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
import lombok.Data;

import java.util.UUID;

@Data
public class TransferAccountInfo {
    private UUID accountId;
    private String accountNumber;
    private CurrencyCode accountCurrencyCode;
}
