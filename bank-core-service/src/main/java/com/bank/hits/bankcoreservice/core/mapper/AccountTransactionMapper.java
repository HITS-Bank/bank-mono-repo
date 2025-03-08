package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import ru.ciklon.bank.bankcoreservice.api.dto.AccountTransactionDto;
import ru.ciklon.bank.bankcoreservice.core.entity.AccountTransaction;

@Mapper
public interface AccountTransactionMapper {
    AccountTransactionDto map(final AccountTransaction tx);

    AccountTransaction map(final AccountTransactionDto dto);
}
