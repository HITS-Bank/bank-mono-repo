package com.bank.hits.bankcreditservice.repository;

import com.bank.hits.bankcreditservice.model.CreditHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreditHistoryRepository extends JpaRepository<CreditHistory, UUID> {

    List<CreditHistory> findByClientUuid(UUID clientUuid);

    Optional<CreditHistory> findByNumber(String number);

    List<CreditHistory> findByRemainingDebtGreaterThan(BigDecimal amount);

    Page<CreditHistory> findByClientUuidAndRemainingDebtGreaterThan(UUID clientUuid, BigDecimal remainingDebt, Pageable pageable);
}
