package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class AccountDto {
    private UUID accountId;
    private String accountNumber;
    private String balance;
    private boolean blocked;
    private boolean closed;
}
