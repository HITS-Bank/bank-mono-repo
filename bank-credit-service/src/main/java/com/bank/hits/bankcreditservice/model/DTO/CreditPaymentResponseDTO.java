package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditPaymentResponseDTO {
    private boolean isApproved;
    private BigDecimal approvedAmount;
}
