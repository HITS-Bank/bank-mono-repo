package com.bank.hits.bankuserservice.common.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.bank.hits.bankuserservice.common.model.UserEntity;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
    Page<UserEntity> findByRole(UserEntity.Role role, PageRequest pageRequest);
    Page<UserEntity> findByFirstNameContainingIgnoreCase(String nameQuery, PageRequest pageRequest);
    Page<UserEntity> findByRoleAndFirstNameContainingIgnoreCase(UserEntity.Role role, String nameQuery, PageRequest pageRequest);
}
