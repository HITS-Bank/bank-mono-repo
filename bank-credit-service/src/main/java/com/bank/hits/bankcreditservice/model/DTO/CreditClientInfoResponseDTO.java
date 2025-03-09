package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CreditClientInfoResponseDTO {
    private UUID clientId;
    private List<AccountInfoDTO> accounts;
    private List<CreditContractDto> credits;
    private List<CreditTransactionDto> creditTransactions;
    private List<AccountTransactionDto> accountTransactions;
}
