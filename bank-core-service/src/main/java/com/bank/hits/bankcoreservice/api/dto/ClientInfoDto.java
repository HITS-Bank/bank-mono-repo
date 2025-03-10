package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ClientInfoDto {
    private UUID clientId;
    private List<AccountDto> accounts;
    private List<CreditContractDto> credits;
    private List<CreditTransactionDto> creditTransactions;
    private List<AccountTransactionDto> accountTransactions;

}
