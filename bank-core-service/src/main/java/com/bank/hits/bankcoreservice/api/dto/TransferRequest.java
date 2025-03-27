package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class TransferRequest {
    private UUID senderAccountId;
    private UUID receiverAccountId;
    private String transferAmount;
}
