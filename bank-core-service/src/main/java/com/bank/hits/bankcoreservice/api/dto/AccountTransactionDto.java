package com.bank.hits.bankcoreservice.api.dto;

import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import com.bank.hits.bankcoreservice.api.enums.OperationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AccountTransactionDto {
    private UUID id;
    private OperationType type;
    private String amount;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonProperty("executedAt")
    private LocalDateTime executedAt;
    private CurrencyCode currencyCode;
}
