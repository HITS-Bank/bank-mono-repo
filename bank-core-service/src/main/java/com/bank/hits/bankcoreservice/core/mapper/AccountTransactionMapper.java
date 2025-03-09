package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.AccountTransactionDto;
import com.bank.hits.bankcoreservice.core.entity.AccountTransaction;
import org.mapstruct.Mapping;

@Mapper
public interface AccountTransactionMapper {

    @Mapping(source = "transactionId", target = "id")
    @Mapping(source = "transactionType", target = "type")
    @Mapping(source = "transactionDate", target = "executedAt")
    AccountTransactionDto map(final AccountTransaction tx);

    AccountTransaction map(final AccountTransactionDto dto);
}
