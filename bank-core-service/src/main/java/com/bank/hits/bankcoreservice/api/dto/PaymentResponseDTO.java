package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResponseDTO {
    private UUID id;
    private String status;
    private LocalDateTime dateTime;
    private BigDecimal amount;
    private String currencyCode;
}
