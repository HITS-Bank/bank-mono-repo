package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountDto {
    Long accountId;
    Long clientId;
    BigDecimal balance;
    boolean blocked;
    boolean closed;

}
