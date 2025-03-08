package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;
import com.bank.hits.bankcoreservice.core.entity.CreditTransaction;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, UUID> {
    List<CreditTransaction> findByCreditContractIn(Collection<CreditContract> creditContracts);
}
