package com.bank.hits.banknotificationservice.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountOperationEvent {

    private OperationType operationType;
    private String userId;
    private BigDecimal operationAmount;
    private BigDecimal accountBalance;
    private CurrencyCode currencyCode;
}
