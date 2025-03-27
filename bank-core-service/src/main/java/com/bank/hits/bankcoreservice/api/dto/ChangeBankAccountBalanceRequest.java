package com.bank.hits.bankcoreservice.api.dto;

import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ChangeBankAccountBalanceRequest {
    @JsonProperty("currencyCode")
    private final CurrencyCode currencyCode;
    @JsonProperty("amount")
    private final String amount;
}
