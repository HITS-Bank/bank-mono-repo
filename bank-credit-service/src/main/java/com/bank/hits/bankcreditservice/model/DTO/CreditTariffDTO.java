package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.UUID;

@Data
@NoArgsConstructor
public class CreditTariffDTO {
    @JsonProperty("id")
    private UUID id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("interestRate")
    private Double interestRate;

    @JsonProperty("createdAt")
    private Timestamp createdAt;
}
