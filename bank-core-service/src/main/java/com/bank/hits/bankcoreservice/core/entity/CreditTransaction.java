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
import com.bank.hits.bankcoreservice.api.enums.CreditTransactionType;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "credit_transactions")
public class CreditTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;

    @ManyToOne
    @JoinColumn(name = "credit_id", nullable = false)
    private CreditContract creditContract;

    private BigDecimal paymentAmount;
    private LocalDateTime paymentDate;
    private CreditTransactionType transactionType;
}
