package ru.hitsbank.user_service.profile.dto;

import ru.hitsbank.user_service.common.model.UserEntity;

public record UserListRequest(
        UserEntity.Role role,
        String nameQuery,
        int pageSize,
        int pageNumber
) {
}
