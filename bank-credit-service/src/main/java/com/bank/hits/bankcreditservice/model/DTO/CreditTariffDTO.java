package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.sql.Timestamp;
import java.util.UUID;

@Data
public class CreditTariffDTO {
    private UUID id;

    private String name;

    private Double interestRate;

    private Timestamp createdAt;
}
