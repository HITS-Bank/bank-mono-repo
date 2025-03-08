package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditApplicationResponseDTO {
    private String number;
    private TariffDTO tariff;
    private BigDecimal amount;
    private int termInMonths;
    private String bankAccountNumber;
    private BigDecimal paymentAmount;
    private BigDecimal paymentSum;
    private LocalDateTime nextPaymentDateTime;
    private BigDecimal currentDebt;

    @Data
    public static class TariffDTO {
        private UUID id;
        private String name;
        private BigDecimal interestRate;
        private LocalDateTime createdAt;
    }
}
