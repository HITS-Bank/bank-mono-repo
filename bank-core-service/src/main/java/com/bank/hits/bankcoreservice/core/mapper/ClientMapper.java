package com.bank.hits.bankcoreservice.core.mapper;

import org.mapstruct.Mapper;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientDto;
import ru.ciklon.bank.bankcoreservice.core.entity.Client;

@Mapper
public interface ClientMapper {

    ClientDto map(Client client);
}
