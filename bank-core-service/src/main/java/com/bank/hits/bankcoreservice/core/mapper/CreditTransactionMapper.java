package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditTransactionDto;
import ru.ciklon.bank.bankcoreservice.core.entity.CreditTransaction;

@Mapper
public interface CreditTransactionMapper {
    CreditTransactionDto map(final CreditTransaction tx);

    CreditTransaction map(final CreditTransactionDto dto);
}
