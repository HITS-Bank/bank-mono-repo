package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class ClientInfoDto {
    private UUID clientId;

    private BigDecimal MasterAccountAmount;
    private int creditRating;
    private List<AccountDto> accounts;
    private List<CreditContractDto> credits;
    private List<CreditTransactionDto> creditTransactions;
    private List<AccountTransactionDto> accountTransactions;

}
