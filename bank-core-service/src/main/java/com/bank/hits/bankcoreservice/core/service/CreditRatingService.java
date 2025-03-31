package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;
import com.bank.hits.bankcoreservice.core.entity.OverduePayment;
import com.bank.hits.bankcoreservice.core.repository.AccountRepository;
import com.bank.hits.bankcoreservice.core.repository.ClientRepository;
import com.bank.hits.bankcoreservice.core.repository.OverduePaymentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CreditRatingService {
    private final OverduePaymentRepository overduePaymentRepository;
    private final ClientRepository clientRepository;

    public CreditRatingService(OverduePaymentRepository overduePaymentRepository,
                               ClientRepository clientRepository) {
        this.overduePaymentRepository = overduePaymentRepository;
        this.clientRepository = clientRepository;
    }

    public void updateCreditRating(Client client, CreditContract creditContract) {
        UUID creditContractId = creditContract.getCreditContractId();
        // Получаем список просроченных платежей для кредитного договора
        List<OverduePayment> overduePayments = overduePaymentRepository.findByCreditContractId(creditContractId);
        int overdueCount = overduePayments.size();
        int ratingChange;

        // Пример бизнес-логики:
        // просроченных платежей 0 -> +5 баллов,
        // 1 платеж -> +3 балла,
        // 2 платежа -> 0,
        // больше двух -> -5 баллов.
        if (overdueCount == 0) {
            ratingChange = 5;
        } else if (overdueCount == 1) {
            ratingChange = 3;
        } else if (overdueCount == 2) {
            ratingChange = 0;
        } else {
            ratingChange = -5;
        }

        int currentRating = client.getCreditRating();
        int newRating = currentRating + ratingChange;
        client.setCreditRating(newRating);
        clientRepository.save(client);
    }
}
