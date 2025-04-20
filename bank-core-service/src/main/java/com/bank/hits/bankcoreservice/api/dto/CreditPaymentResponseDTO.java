package com.bank.hits.bankcoreservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;


@Data
@AllArgsConstructor
public class CreditPaymentResponseDTO {
    @JsonProperty("isApproved")
    private boolean isApproved;
    @JsonProperty("approvedAmount")
    private BigDecimal approvedAmount;
}
