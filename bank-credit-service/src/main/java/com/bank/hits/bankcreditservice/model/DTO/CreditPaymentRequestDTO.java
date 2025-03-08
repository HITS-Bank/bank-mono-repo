package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditPaymentRequestDTO {
    private String loanNumber;
    private BigDecimal paymentAmount;
}
