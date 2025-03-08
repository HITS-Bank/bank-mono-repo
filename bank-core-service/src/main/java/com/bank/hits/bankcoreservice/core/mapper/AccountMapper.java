package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.core.entity.Account;

@Mapper
public interface AccountMapper {

    AccountDto map(final Account account);
}
