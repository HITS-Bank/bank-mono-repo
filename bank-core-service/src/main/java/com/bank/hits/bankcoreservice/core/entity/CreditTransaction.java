package com.bank.hits.bankcoreservice.core.entity;

import com.bank.hits.bankcoreservice.api.dto.PaymentStatus;
import jakarta.persistence.*;
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
    @Column(name = "paymentStatus")
    private PaymentStatus paymentStatus;
    private BigDecimal paymentAmount;
    private LocalDateTime paymentDate;
    private CreditTransactionType transactionType;
}
