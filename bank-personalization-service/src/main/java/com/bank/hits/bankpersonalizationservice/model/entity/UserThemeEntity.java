package com.bank.hits.bankpersonalizationservice.model.entity;

import com.bank.hits.bankpersonalizationservice.common.enums.Role;
import com.bank.hits.bankpersonalizationservice.common.enums.Theme;
import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Table(name = "user_theme")
@Data
public class UserThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Theme theme;
}
