package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditContractDto {
    private UUID creditContractId;
    private BigDecimal creditAmount;
    private BigDecimal creditRate;
    private BigDecimal creditRepaymentAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
