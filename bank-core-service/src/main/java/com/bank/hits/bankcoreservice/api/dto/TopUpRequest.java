package com.bank.hits.bankcoreservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class TopUpRequest {
    @JsonProperty("amount")
    private final String amount;

    @JsonProperty("currencyCode")
    private CurrencyCode currencyCode;
}
