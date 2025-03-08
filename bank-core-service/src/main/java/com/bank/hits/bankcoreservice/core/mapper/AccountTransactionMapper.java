package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.AccountTransactionDto;
import com.bank.hits.bankcoreservice.core.entity.AccountTransaction;

@Mapper
public interface AccountTransactionMapper {
    AccountTransactionDto map(final AccountTransaction tx);

    AccountTransaction map(final AccountTransactionDto dto);
}
