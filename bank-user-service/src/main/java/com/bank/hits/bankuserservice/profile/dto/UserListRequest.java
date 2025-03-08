package com.bank.hits.bankuserservice.profile.dto;

import com.bank.hits.bankuserservice.common.model.UserEntity;

public record UserListRequest(
        UserEntity.Role role,
        String nameQuery,
        int pageSize,
        int pageNumber
) {
}
