package com.bank.hits.bankcoreservice.core.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.Client;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "credit_contracts")
public class CreditContract {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID creditContractId;
    private BigDecimal creditAmount;
    private BigDecimal remainingAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdDate;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;


}
