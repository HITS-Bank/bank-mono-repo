package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransferRequest {
    private UUID requestId;
    private UUID senderAccountId;
    private String receiverAccountNumber;
    private String transferAmount;
}
