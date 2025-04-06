package com.bank.hits.bankpersonalizationservice.repository;

import com.bank.hits.bankpersonalizationservice.model.entity.HiddenAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HiddenAccountsRepository extends JpaRepository<HiddenAccountEntity, UUID> {

    Optional<HiddenAccountEntity> findByUserIdAndAccountId(String userId, UUID accountId);
    List<HiddenAccountEntity> findAllByUserId(String userId);
}
