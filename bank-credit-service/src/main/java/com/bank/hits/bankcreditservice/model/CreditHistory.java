package com.bank.hits.bankcreditservice.model;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credit_history")
@Data
public class CreditHistory {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "tariff_id", nullable = false)
    private UUID tariffId;

    @Column(name = "bankAccountNumber", nullable = false)
    private String bankAccountNumber;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "client_uuid", nullable = false)
    private UUID clientUuid;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "monthly_payment", nullable = false)
    private BigDecimal monthlyPayment;

    @CreationTimestamp
    @Column(name = "start_date", nullable = false, updatable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "remaining_debt", nullable = false)
    private BigDecimal remainingDebt;
}
