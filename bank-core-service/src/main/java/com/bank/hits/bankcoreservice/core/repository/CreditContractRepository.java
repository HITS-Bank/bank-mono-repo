package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditContractRepository extends JpaRepository<CreditContract, UUID> {

    Optional<CreditContract> findByCreditApprovedId(UUID creditApprovedId);

    List<CreditContract> findByClient(final Client client);

}
