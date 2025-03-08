package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class VerificationAnswerDTO {
    @JsonProperty("isBlocked")
    private boolean isBlocked;
}
