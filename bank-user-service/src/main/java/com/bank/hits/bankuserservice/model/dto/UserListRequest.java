package com.bank.hits.bankuserservice.model.dto;

import com.bank.hits.bankuserservice.common.enums.Role;
import jakarta.validation.constraints.Positive;

public record UserListRequest(
        Role role,
        String nameQuery,
        int pageSize,
        @Positive int pageNumber
) {
}
