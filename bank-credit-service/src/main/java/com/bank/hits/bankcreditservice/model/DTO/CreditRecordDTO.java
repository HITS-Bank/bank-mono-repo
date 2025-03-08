package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreditRecordDTO {
    private UUID creditId;
    private BigDecimal amount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
