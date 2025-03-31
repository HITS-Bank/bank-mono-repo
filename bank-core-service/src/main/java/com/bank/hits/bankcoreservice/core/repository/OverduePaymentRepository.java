package com.bank.hits.bankcoreservice.core.repository;

import com.bank.hits.bankcoreservice.core.entity.OverduePayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OverduePaymentRepository extends JpaRepository<OverduePayment, UUID> {
    List<OverduePayment> findAll();
    List<OverduePayment> findByCreditContractId(UUID creditContractId);

}
