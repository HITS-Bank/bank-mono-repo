package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreditPaymentResponseDTO {
    @JsonProperty("isApproved")
    private boolean isApproved;
    @JsonProperty("approvedAmount")
    private BigDecimal approvedAmount;
}
