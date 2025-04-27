package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreditPaymentRequestDTO {
    private UUID requestId;
    private BigDecimal amount;
}
