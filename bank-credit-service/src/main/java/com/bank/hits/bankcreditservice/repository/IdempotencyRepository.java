package com.bank.hits.bankcreditservice.repository;

import java.util.UUID;

import com.bank.hits.bankcreditservice.model.IdempotentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotentResponse, UUID> {
}
