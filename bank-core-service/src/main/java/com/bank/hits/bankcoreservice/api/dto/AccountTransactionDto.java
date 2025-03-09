package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;
import com.bank.hits.bankcoreservice.api.enums.OperationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountTransactionDto {
    private UUID id;
    private OperationType type;
    private String amount;
    private LocalDateTime executedAt;
}
