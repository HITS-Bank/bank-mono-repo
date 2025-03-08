package com.bank.hits.bankcoreservice.core.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditApprovedDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditContractDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditTransactionDto;
import ru.ciklon.bank.bankcoreservice.api.enums.AccountType;
import ru.ciklon.bank.bankcoreservice.api.enums.CreditTransactionType;
import ru.ciklon.bank.bankcoreservice.config.service.KafkaProducerService;
import ru.ciklon.bank.bankcoreservice.core.entity.Account;
import ru.ciklon.bank.bankcoreservice.core.entity.Client;
import ru.ciklon.bank.bankcoreservice.core.entity.CreditContract;
import ru.ciklon.bank.bankcoreservice.core.entity.CreditTransaction;
import ru.ciklon.bank.bankcoreservice.core.mapper.CreditContractMapper;
import ru.ciklon.bank.bankcoreservice.core.mapper.CreditTransactionMapper;
import ru.ciklon.bank.bankcoreservice.core.repository.AccountRepository;
import ru.ciklon.bank.bankcoreservice.core.repository.ClientRepository;
import ru.ciklon.bank.bankcoreservice.core.repository.CreditContractRepository;
import ru.ciklon.bank.bankcoreservice.core.repository.CreditTransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditService {

    private final CreditContractRepository creditContractRepository;
    private final CreditTransactionMapper creditTransactionMapper;
    private final CreditContractMapper creditContractMapper;

    private final CreditTransactionRepository creditContractTransactionRepository;
    private final ClientRepository clientRepository;
    private final AccountRepository accountRepository;
    private final KafkaProducerService kafkaProducerService;

    public List<CreditContractDto> getCreditsByClientId(final UUID clientId) {
        final var client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return creditContractRepository.findByClient(client).stream()
                .map(creditContractMapper::map)
                .toList();
    }

    public List<CreditTransactionDto> getCreditContractTransactionsByClientId(final UUID clientId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        final List<CreditContract> creditContracts = creditContractRepository.findByClient(client);
        return creditContractTransactionRepository.findByCreditContractIn(creditContracts).stream()
                .map(creditTransactionMapper::map)
                .toList();
    }

    @Transactional
    public void processCreditApproval(final CreditApprovedDto creditApprovedDto) {
        log.info("Processing credit approval for client {}", creditApprovedDto.getClientId());

        final Client client = clientRepository.findById(creditApprovedDto.getClientId())
                        .orElse(clientRepository.save(new Client(creditApprovedDto.getClientId())));

        final Account creditAccount = accountRepository.findByClientAndAccountType(client, AccountType.CREDIT)
                .orElseGet(() -> createCreditAccount(client));

        CreditContract creditContract = new CreditContract();
        creditContract.setCreditAmount(creditApprovedDto.getApprovedAmount());
        creditContract.setRemainingAmount(creditApprovedDto.getApprovedAmount());
        creditContract.setStartDate(LocalDateTime.now());
        creditContract.setAccount(creditAccount);
        creditContract.setClient(client);
        creditContract = creditContractRepository.save(creditContract);

        final CreditTransaction transaction = new CreditTransaction();
        transaction.setCreditContract(creditContract);
        transaction.setPaymentAmount(creditApprovedDto.getApprovedAmount());
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setTransactionType(CreditTransactionType.CREDIT_DEPOSIT);
        creditContractTransactionRepository.save(transaction);

        kafkaProducerService.sendCreditAccountCreatedEvent(creditContract, creditAccount);
    }

    private Account createCreditAccount(final Client client) {
        final Account newAccount = new Account();
        newAccount.setClient(client);
        newAccount.setAccountType(AccountType.CREDIT);
        newAccount.setBalance(BigDecimal.ZERO);
        return accountRepository.save(newAccount);
    }

}
