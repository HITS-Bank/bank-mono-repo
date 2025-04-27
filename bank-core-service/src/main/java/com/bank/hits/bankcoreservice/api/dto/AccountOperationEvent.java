package com.bank.hits.bankcoreservice.api.dto;



import com.bank.hits.bankcoreservice.api.enums.OperationType;
import lombok.Builder;
import lombok.Data;
import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
import java.math.BigDecimal;

@Data
@Builder
public class AccountOperationEvent {

    private OperationType operationType;
    private String userId;
    private BigDecimal operationAmount;
    private BigDecimal accountBalance;
    private CurrencyCode currencyCode;
}

