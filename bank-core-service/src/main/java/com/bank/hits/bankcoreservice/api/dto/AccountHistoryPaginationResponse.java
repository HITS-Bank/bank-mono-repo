package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccountHistoryPaginationResponse {
    private final PageInfo pageInfo;
    private final List<AccountTransactionDto> accountTransactions;
}
