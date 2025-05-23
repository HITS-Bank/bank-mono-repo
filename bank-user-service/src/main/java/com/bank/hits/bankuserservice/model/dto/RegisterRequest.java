package com.bank.hits.bankuserservice.model.dto;

import com.bank.hits.bankuserservice.common.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
public class RegisterRequest {

    private UUID requestId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String password;

    @NotEmpty
    private List<Role> roles;
}