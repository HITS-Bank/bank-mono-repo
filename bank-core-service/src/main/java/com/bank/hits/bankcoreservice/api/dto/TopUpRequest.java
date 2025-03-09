package com.bank.hits.bankcoreservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TopUpRequest {
    @JsonProperty("accountNumber")
    private final String accountNumber;
    @JsonProperty("amount")
    private final String amount;
}
