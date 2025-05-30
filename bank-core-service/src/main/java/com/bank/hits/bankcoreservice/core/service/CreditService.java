package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.dto.CurrencyCode;
import com.bank.hits.bankcoreservice.core.utils.AccountNumberGenerator;
import com.bank.hits.bankcoreservice.core.utils.IdempotencyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bank.hits.bankcoreservice.api.dto.CreditApprovedDto;
import com.bank.hits.bankcoreservice.api.dto.CreditContractDto;
import com.bank.hits.bankcoreservice.api.dto.CreditTransactionDto;
import com.bank.hits.bankcoreservice.api.enums.AccountType;
import com.bank.hits.bankcoreservice.config.service.KafkaProducerService;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;
import com.bank.hits.bankcoreservice.core.entity.CreditTransaction;
import com.bank.hits.bankcoreservice.core.mapper.CreditContractMapper;
import com.bank.hits.bankcoreservice.core.mapper.CreditTransactionMapper;
import com.bank.hits.bankcoreservice.core.repository.AccountRepository;
import com.bank.hits.bankcoreservice.core.repository.ClientRepository;
import com.bank.hits.bankcoreservice.core.repository.CreditContractRepository;
import com.bank.hits.bankcoreservice.core.repository.CreditTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {
    private final String MASTER_ACCOUNT_NUMBER = "00000000000000000001";

    private final CreditContractRepository creditContractRepository;
    private final CreditTransactionMapper creditTransactionMapper;
    private final CreditContractMapper creditContractMapper;

    private final CreditTransactionRepository creditContractTransactionRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;
    private final AccountNumberGenerator accountNumberGenerator;
    private final IdempotencyUtils idempotency;


    public List<CreditContractDto> getCreditsByClientId(final UUID clientId) {
        final var client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return creditContractRepository.findByClient(client).stream()
                .sorted(Comparator.comparing(CreditContract::getCreatedDate, Comparator.nullsLast(Comparator.reverseOrder())).reversed())
                .map(creditContractMapper::map)
                .toList();
    }

    public List<CreditTransactionDto> getCreditContractTransactionsByClientId(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        final List<CreditContract> creditContracts = creditContractRepository.findByClient(client);
        return creditContractTransactionRepository.findByCreditContractIn(creditContracts).stream()
                .sorted(Comparator.comparing(CreditTransaction::getPaymentDate, Comparator.nullsLast(Comparator.reverseOrder())).reversed())
                .map(creditTransactionMapper::map)
                .toList();
    }

    @Transactional
    public void processCreditApproval(final CreditApprovedDto creditApprovedDto, UUID correlationId, UUID requestId) {
        try {
            log.info("Processing credit approval for client {}", creditApprovedDto.getClientId());

            final Client client = clientRepository.findByClientId(creditApprovedDto.getClientId())
                    .orElseGet(() -> {
                        try {
                            return clientRepository.insertIfNotExists(creditApprovedDto.getClientId()).get();
                        } catch (DataIntegrityViolationException e) {
                            return clientRepository.findByClientId(creditApprovedDto.getClientId()).orElseThrow();
                        }
                    });
            log.info("client id : {}", client.getClientId());

            final Account creditAccount = accountRepository.findById(creditApprovedDto.getAccountId())
                    .orElseGet(() -> createCreditAccount(client));

            log.info("до masterAccount");
            Account masterAccount = accountRepository.findByAccountNumber(MASTER_ACCOUNT_NUMBER)
                    .orElseThrow(() -> new IllegalStateException("Master account not found"));

            log.info("Мастер аккаунт найден");
            final BigDecimal approvedAmount = creditApprovedDto.getApprovedAmount();

            if (masterAccount.getBalance().compareTo(approvedAmount) < 0) {
                throw new IllegalStateException("Недостаточно средств на мастер-счете");
            }
            masterAccount.setBalance(masterAccount.getBalance().subtract(approvedAmount));
            accountRepository.save(masterAccount);

            CreditContract creditContract = new CreditContract();
            creditContract.setCreditApprovedId(creditApprovedDto.getCreditId());
            creditContract.setCreditAmount(creditApprovedDto.getApprovedAmount());
            creditContract.setRemainingAmount(creditApprovedDto.getApprovedAmount());
            creditContract.setStartDate(LocalDateTime.now());
            creditContract.setAccount(creditAccount);
            creditContract.setClient(client);
            creditContract = creditContractRepository.save(creditContract);

            /*
            final CreditTransaction transaction = new CreditTransaction();
            transaction.setCreditContract(creditContract);
            transaction.setCreditContractId(creditContract.getCreditContractId());
            transaction.setPaymentAmount(creditApprovedDto.getApprovedAmount());
            transaction.setPaymentDate(LocalDateTime.now());
            transaction.setTransactionType(CreditTransactionType.CREDIT_DEPOSIT);
            creditContractTransactionRepository.save(transaction);

             */

            creditAccount.setBalance(creditAccount.getBalance().add(creditContract.getCreditAmount()));
            log.info("Перед accountRepository.save(creditAccount)");
            accountRepository.save(creditAccount);
            idempotency.storeResponse(requestId, true);
            kafkaProducerService.sendCreditApproved(true,correlationId);
            kafkaProducerService.sendCreditAccountCreatedEvent(creditContract, creditAccount);
        }
        catch (Exception e)
        {
            log.info("Внутренняя ошибка выдачи кредита: {} ", e.getMessage());
            idempotency.storeResponse(requestId, false);
            kafkaProducerService.sendCreditApproved(false,correlationId);
        }
    }

    private Account createCreditAccount(final Client client) {
        final Account newAccount = new Account();
        newAccount.setCurrencyCode(CurrencyCode.RUB);
        newAccount.setClient(client);
        newAccount.setAccountType(AccountType.CREDIT);
        newAccount.setBalance(BigDecimal.ZERO);
        newAccount.setAccountNumber(accountNumberGenerator.generateAccountNumber());
        return accountRepository.save(newAccount);
    }

}
