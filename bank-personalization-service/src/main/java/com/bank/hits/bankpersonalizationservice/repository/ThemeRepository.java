package com.bank.hits.bankpersonalizationservice.repository;

import com.bank.hits.bankpersonalizationservice.common.enums.Role;
import com.bank.hits.bankpersonalizationservice.model.entity.UserThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThemeRepository extends JpaRepository<UserThemeEntity, UUID> {

    Optional<UserThemeEntity> findByUserIdAndChannel(String userId, Role channel);
}