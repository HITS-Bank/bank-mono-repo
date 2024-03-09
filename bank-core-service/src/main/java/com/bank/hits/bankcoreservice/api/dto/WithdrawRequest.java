package com.bank.hits.bankcoreservice.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class WithdrawRequest {
    @JsonProperty("accountNumber")
    private final String accountNumber;
    @JsonProperty("amount")
    private final String amount;
}
