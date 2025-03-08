package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import ru.ciklon.bank.bankcoreservice.api.dto.AccountDto;
import ru.ciklon.bank.bankcoreservice.core.entity.Account;

@Mapper
public interface AccountMapper {

    AccountDto map(final Account account);
}
