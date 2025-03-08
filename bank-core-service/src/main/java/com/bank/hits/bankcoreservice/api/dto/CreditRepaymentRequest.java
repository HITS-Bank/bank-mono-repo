package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditRepaymentRequest {
    private UUID creditContractId;
    private String creditAmount;
    private LocalDateTime enrollmentDate;
}
