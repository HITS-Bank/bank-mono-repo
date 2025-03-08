package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.CreditTransactionDto;
import com.bank.hits.bankcoreservice.core.entity.CreditTransaction;

@Mapper
public interface CreditTransactionMapper {
    CreditTransactionDto map(final CreditTransaction tx);

    CreditTransaction map(final CreditTransactionDto dto);
}
