package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

@Data
public class CloseAccountRequest {
    private String accountNumber;
}
