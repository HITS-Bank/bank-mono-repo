package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.DTO.CreditPaymentRequestDTO;
import com.bank.hits.bankcreditservice.model.DTO.PaymentStatus;

public interface CreditPaymentService {
    boolean processPayment(CreditPaymentRequestDTO request, PaymentStatus paymentStatus) throws Exception;
}
