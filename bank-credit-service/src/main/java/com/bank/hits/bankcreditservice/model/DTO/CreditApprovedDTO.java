package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditApprovedDTO {
    private UUID clientId;
    private BigDecimal approvedAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime approvedDate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
