package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.util.UUID;

@Data
public class AccountInfoDTO {
    private UUID accountId;
    private String accountNumber;
    private String balance;
    private boolean blocked;
    private boolean closed;
}
