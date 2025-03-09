package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class AccountHistoryPaginationResponse {
    private final PageInfo pageInfo;
    private final List<AccountTransactionDto> accountTransactions;
}
