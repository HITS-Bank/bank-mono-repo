package com.bank.hits.banknotificationservice.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterFcmRequest {

    @NotBlank
    private String fcmToken;
}
