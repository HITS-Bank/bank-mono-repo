package com.bank.hits.bankuserservice.user_service.mapper;

import com.bank.hits.bankuserservice.common.dto.UserDto;
import com.bank.hits.bankuserservice.common.enums.Role;
import com.bank.hits.bankuserservice.common.model.KeycloakUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserMapper {

    public UserDto toDto(KeycloakUserResponse keycloakUser, List<Role> roles) {
        UserDto userDto = new UserDto();
        userDto.setId(keycloakUser.getId());
        userDto.setFirstName(keycloakUser.getFirstName());
        userDto.setLastName(keycloakUser.getLastName());
        userDto.setIsBlocked(keycloakUser.getAttributes() != null &&
                keycloakUser.getAttributes().getIsBanned() != null &&
                !keycloakUser.getAttributes().getIsBanned().isEmpty() &&
                Boolean.parseBoolean(keycloakUser.getAttributes().getIsBanned().get(0)));
        userDto.setRoles(roles);
        return userDto;
    }
}
