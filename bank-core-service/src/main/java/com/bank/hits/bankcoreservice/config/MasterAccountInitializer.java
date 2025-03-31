package com.bank.hits.bankcoreservice.config;

import com.bank.hits.bankcoreservice.api.dto.CurrencyCode;
import com.bank.hits.bankcoreservice.api.enums.AccountType;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.repository.AccountRepository;
import com.bank.hits.bankcoreservice.core.repository.ClientRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class MasterAccountInitializer {
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;

    private static final String MASTER_ACCOUNT_NUMBER = "MASTER-0000000001";
    private static final UUID MASTER_CLIENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @PostConstruct
    public void init() {
        Optional<Account> existing = accountRepository.findByAccountNumber(MASTER_ACCOUNT_NUMBER);
        if (((Optional<?>) existing).isPresent()) {
            log.info("Мастер-счет уже существует: {}", MASTER_ACCOUNT_NUMBER);
            return;
        }

        Client bankClient = clientRepository.findByClientId(MASTER_CLIENT_ID)
                .orElseGet(() -> {
                    Client newBankClient = new Client();
                    newBankClient.setClientId(MASTER_CLIENT_ID);
                    newBankClient.setCreditRating(0);
                    clientRepository.save(newBankClient);
                    log.info("Создан клиент банка с ID {}", MASTER_CLIENT_ID);
                    return newBankClient;
                });

        Account masterAccount = new Account();
        masterAccount.setClient(bankClient);
        masterAccount.setAccountNumber(MASTER_ACCOUNT_NUMBER);
        masterAccount.setCurrencyCode(CurrencyCode.RUB);
        masterAccount.setAccountType(AccountType.MASTER);
        masterAccount.setBalance(BigDecimal.valueOf(100000));
        masterAccount.setBlocked(false);
        masterAccount.setClosed(false);

        accountRepository.save(masterAccount);
        log.info("Мастер-счет успешно создан: {}", masterAccount.getAccountNumber());
    }
}
