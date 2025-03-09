package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CreditPaymentResponseDTO {
    private boolean isApproved;
    private String approvedAmount;
}
