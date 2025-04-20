package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.hits.bankcoreservice.api.enums.AccountType;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.Client;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {
    Page<Account> findByClient(final Client client, Pageable pageable);
    List<Account> findByClient(final Client client);
    Optional<Account> findByClientAndAccountType(final Client client, final AccountType accountType);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> findById(UUID accountId);

}
