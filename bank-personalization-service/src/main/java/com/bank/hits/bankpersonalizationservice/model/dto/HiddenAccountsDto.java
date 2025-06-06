package com.bank.hits.bankpersonalizationservice.model.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HiddenAccountsDto {

    private List<UUID> accounts;
}
