package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;
import com.bank.hits.bankcoreservice.api.enums.AccountTransactionType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountTransactionDto {
    private UUID accountTransactionId;
    private UUID accountId;
    private AccountTransactionType type;
    private String amount;
    private LocalDateTime transactionDate;
}
