package com.bank.hits.bankcreditservice.repository;

import com.bank.hits.bankcreditservice.model.CreditTariff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditTariffRepository extends JpaRepository<CreditTariff, UUID>{
    Page<CreditTariff> findByRelevanceTrueAndNameContainingIgnoreCase(String name, Pageable pageable);
}
