package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AccountsPaginationResponse {
    private PageInfo pageInfo;
    private List<AccountDto> accounts;

}
