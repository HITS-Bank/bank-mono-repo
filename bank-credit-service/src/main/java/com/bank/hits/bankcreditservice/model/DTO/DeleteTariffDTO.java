package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.UUID;

@Data
public class DeleteTariffDTO {
    @JsonProperty("tariffId")
    private UUID tariffId;
}
