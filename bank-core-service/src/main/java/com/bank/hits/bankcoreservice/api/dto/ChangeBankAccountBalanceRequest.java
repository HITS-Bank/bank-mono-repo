package com.bank.hits.bankcoreservice.api.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ChangeBankAccountBalanceRequest {
    @JsonProperty("requestId")
    private final UUID requestId;
    @JsonProperty("currencyCode")
    private final CurrencyCode currencyCode;
    @JsonProperty("amount")
    private final String amount;
}
