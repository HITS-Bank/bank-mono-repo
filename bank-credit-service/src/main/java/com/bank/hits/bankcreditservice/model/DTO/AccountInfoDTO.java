package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountInfoDTO {
    private Long accountId;
    private Long clientId;
    private BigDecimal balance;
    private boolean blocked;
    private boolean closed;
}
