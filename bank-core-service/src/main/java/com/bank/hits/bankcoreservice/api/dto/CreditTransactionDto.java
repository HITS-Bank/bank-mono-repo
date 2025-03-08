package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditTransactionDto {
    private UUID creditTransactionId;
    private UUID creditContractId;
    private String paymentAmount;
    private LocalDateTime paymentDate;
}
