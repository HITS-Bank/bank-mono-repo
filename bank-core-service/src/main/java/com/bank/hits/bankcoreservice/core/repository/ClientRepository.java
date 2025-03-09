package com.bank.hits.bankcoreservice.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.hits.bankcoreservice.core.entity.Client;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
    Optional<Client> findByClientId(UUID clientId);
}
