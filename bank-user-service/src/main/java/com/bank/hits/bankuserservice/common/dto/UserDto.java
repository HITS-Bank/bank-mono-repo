package com.bank.hits.bankuserservice.common.dto;

import com.bank.hits.bankuserservice.common.model.UserEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class UserDto {

    @NotBlank
    private UUID id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String email;

    @NotNull
    private UserEntity.Role role;

    private Boolean isBanned;
}