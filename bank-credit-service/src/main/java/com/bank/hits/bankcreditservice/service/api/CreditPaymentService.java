package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.DTO.CreditPaymentRequestDTO;

public interface CreditPaymentService {
    boolean processPayment(CreditPaymentRequestDTO request) throws Exception;
}
