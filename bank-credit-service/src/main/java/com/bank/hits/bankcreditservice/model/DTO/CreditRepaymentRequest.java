package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditRepaymentRequest {
    private UUID creditContractId;
    private String creditAmount;
    private LocalDateTime enrollmentDate;
    private PaymentStatus paymentStatus;
}
