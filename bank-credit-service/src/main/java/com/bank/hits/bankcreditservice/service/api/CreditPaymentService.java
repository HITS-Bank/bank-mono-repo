package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.DTO.CreditPaymentRequestDTO;
import com.bank.hits.bankcreditservice.model.DTO.PaymentStatus;

import java.util.UUID;

public interface CreditPaymentService {
    boolean processPayment(UUID loanId, CreditPaymentRequestDTO request, PaymentStatus paymentStatus) throws Exception;
}
