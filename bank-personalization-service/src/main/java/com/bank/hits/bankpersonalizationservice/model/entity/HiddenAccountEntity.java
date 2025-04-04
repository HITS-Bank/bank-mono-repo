package com.bank.hits.bankpersonalizationservice.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "hidden_accounts")
@Data
public class HiddenAccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;
}
