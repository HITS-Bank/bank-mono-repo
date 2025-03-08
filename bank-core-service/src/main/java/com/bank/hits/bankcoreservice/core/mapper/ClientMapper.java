package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import com.bank.hits.bankcoreservice.api.dto.ClientDto;
import com.bank.hits.bankcoreservice.core.entity.Client;

@Mapper
public interface ClientMapper {

    ClientDto map(Client client);
}
