package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ciklon.bank.bankcoreservice.api.enums.AccountType;
import ru.ciklon.bank.bankcoreservice.core.entity.Account;
import ru.ciklon.bank.bankcoreservice.core.entity.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByClientId(final UUID clientId);

    Optional<Account> findByClientAndAccountType(final Client client, final AccountType accountType);

    Optional<Account> findByAccountNumber(String accountNumber);
}
