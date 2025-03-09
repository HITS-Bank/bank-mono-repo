package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

@Data
public class TopUpRequest {
    private final String accountNumber;
    private final String amount;
}
