package ru.hitsbank.user_service.common.mapper;

import org.mapstruct.Mapper;
import ru.hitsbank.user_service.common.dto.UserDto;
import ru.hitsbank.user_service.common.model.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(UserEntity entity);
}
