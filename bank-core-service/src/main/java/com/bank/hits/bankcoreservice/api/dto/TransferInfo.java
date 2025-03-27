package com.bank.hits.bankcoreservice.api.dto;

import lombok.Data;

@Data
public class TransferInfo {
    private TransferAccountInfo senderAccountInfo;
    private TransferAccountInfo receiverAccountInfo;
    private String transferAmountBeforeConversion;
    private String transferAmountAfterConversion;
}
