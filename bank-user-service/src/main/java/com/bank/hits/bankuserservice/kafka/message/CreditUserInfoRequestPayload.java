package com.bank.hits.bankuserservice.kafka.message;


import lombok.Data;

@Data
public record CreditUserInfoRequestPayload(
        String userId
) {
}
