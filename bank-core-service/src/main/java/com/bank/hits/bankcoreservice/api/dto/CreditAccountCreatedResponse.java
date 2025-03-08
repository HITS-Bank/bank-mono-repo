package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CreditAccountCreatedResponse {
    private UUID creditId;
    private UUID accountId;
    private String creditAmount;
}
