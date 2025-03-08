package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreditClientInfoResponseDTO {
    private UUID userId;
    private List<AccountInfoDTO> accounts;
    private CreditHistoryDTO creditHistory;
}
