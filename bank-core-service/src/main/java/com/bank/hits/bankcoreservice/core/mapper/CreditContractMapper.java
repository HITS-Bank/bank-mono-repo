package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.CreditContractDto;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;

@Mapper
public interface CreditContractMapper {

    CreditContract map(final CreditContractDto creditContractDto);

    CreditContractDto map(final CreditContract creditContract);
}
