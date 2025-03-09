package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.core.entity.Account;
import org.mapstruct.Mapping;

@Mapper
public interface AccountMapper {

    @Mapping(source = "id", target = "accountId")
    AccountDto map(final Account account);
}
