package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreditApplicationRequestDTO {
    private UUID tariffId;
    private BigDecimal amount;
    private int termInMonths;
    private String bankAccountNumber;
}
