package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ExternalTransferRequest {
    private UUID fromAccountId;
    private UUID toClientId;
    private UUID toAccountId;
    private BigDecimal amount;
}
