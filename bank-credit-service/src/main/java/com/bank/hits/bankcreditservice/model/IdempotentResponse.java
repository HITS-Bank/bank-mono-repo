package com.bank.hits.bankcreditservice.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "idempotent_responses")
public class IdempotentResponse {

    @Id
    private UUID id;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "response_status")
    private int responseStatus;

    @Column(name = "response_headers")
    private String responseHeaders;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
