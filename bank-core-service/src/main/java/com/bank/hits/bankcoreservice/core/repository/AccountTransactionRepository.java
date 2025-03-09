package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.hits.bankcoreservice.core.entity.AccountTransaction;

import java.util.List;
import java.util.UUID;

@Repository
public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, UUID> {
    List<AccountTransaction> findByAccountId(final UUID id);
    Page<AccountTransaction> findByAccountId(final UUID id, final Pageable pageable);
}
