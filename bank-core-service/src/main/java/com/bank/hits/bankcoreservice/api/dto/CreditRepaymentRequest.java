package com.bank.hits.bankcoreservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class CreditRepaymentRequest {
    @JsonProperty("creditContractId")
    private UUID creditContractId;
    @JsonProperty("creditAmount")
    private String creditAmount;
    @JsonProperty("enrollmentDate")
    private LocalDateTime enrollmentDate;

    @JsonProperty("paymentStatus")
    private PaymentStatus paymentStatus;
}
