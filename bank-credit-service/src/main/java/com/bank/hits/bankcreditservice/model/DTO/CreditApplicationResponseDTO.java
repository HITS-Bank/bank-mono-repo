package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditApplicationResponseDTO {
    private UUID id;
    private CurrencyCode currencyCode;
    private String number;
    private TariffDTO tariff;
    private BigDecimal amount;
    private int termInMonths;
    private String bankAccountNumber;
    private UUID bankAccountId;
    private BigDecimal paymentAmount;
    private BigDecimal paymentSum;
    private LocalDateTime nextPaymentDateTime;
    private BigDecimal currentDebt;

    @Data
    public static class TariffDTO {
        private UUID id;
        private String name;
        private Double interestRate;
        private LocalDateTime createdAt;
    }
}
