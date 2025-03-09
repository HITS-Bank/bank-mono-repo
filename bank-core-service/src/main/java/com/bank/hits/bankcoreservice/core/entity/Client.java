package com.bank.hits.bankcoreservice.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import lombok.With;

import java.util.UUID;

@Setter
@Data
@Entity
@With
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID clientId;

    private final boolean isBlocked;

    public Client(final UUID clientId) {
        this.clientId = clientId;
        this.isBlocked = false;
    }

    public Client(final boolean isBlocked) {
        this.isBlocked = isBlocked;
    }

    public Client() {
        this.isBlocked = false;
    }

}
