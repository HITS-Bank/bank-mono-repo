package com.bank.hits.bankcoreservice.core.repository;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.hits.bankcoreservice.core.entity.Client;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    @Transactional
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Client> findByClientId(UUID clientId);

    @Query(value = "INSERT INTO client (client_id, is_blocked) VALUES (:clientId, false) ON CONFLICT (client_id) DO NOTHING RETURNING *", nativeQuery = true)
    Optional<Client> insertIfNotExists(@Param("clientId") UUID clientId);
}
