package com.bank.hits.bankcoreservice.core.repository;

import java.util.UUID;

import com.bank.hits.bankcoreservice.core.entity.IdempotentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyRepository extends JpaRepository<IdempotentResponse, UUID> {
}
