package com.bank.hits.bankuserservice.common.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.model.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(UserEntity entity);
}
