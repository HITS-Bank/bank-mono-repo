package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class InternalTransferRequest {
    private UUID fromAccountId;
    private UUID toAccountId;
    private BigDecimal amount;
}
