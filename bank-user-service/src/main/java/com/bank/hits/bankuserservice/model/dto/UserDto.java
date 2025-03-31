package com.bank.hits.bankuserservice.model.dto;

import com.bank.hits.bankuserservice.common.enums.Role;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {

    private String id;
    private String firstName;
    private String lastName;
    private List<Role> roles;
    private Boolean isBlocked;
}