package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class AccountsPaginationResponse {
    private PageInfo pageInfo;
    private List<AccountDto> accounts;

}
