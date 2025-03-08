package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.ciklon.bank.bankcoreservice.core.entity.Client;
import ru.ciklon.bank.bankcoreservice.core.entity.CreditContract;

import java.util.List;
import java.util.UUID;

@Repository
public interface CreditContractRepository extends JpaRepository<CreditContract, UUID> {


    List<CreditContract> findByClient(final Client client);
}
