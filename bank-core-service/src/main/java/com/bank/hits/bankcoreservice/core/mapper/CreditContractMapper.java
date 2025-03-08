package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditContractDto;
import ru.ciklon.bank.bankcoreservice.core.entity.CreditContract;

@Mapper
public interface CreditContractMapper {

    CreditContract map(final CreditContractDto creditContractDto);

    CreditContractDto map(final CreditContract creditContract);
}
