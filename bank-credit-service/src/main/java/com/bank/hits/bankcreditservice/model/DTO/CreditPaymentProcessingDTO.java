package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreditPaymentProcessingDTO {
    private UUID creditApplicationId;
    private BigDecimal creditAmount;
    private EnrollmentType type = EnrollmentType.MANUAL;

    public enum EnrollmentType {
        MANUAL, AUTO
    }
}
