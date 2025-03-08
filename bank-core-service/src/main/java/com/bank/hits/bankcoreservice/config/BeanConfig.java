package com.bank.hits.bankcoreservice.config;

import org.mapstruct.factory.Mappers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.ciklon.bank.bankcoreservice.core.mapper.AccountMapper;
import ru.ciklon.bank.bankcoreservice.core.mapper.AccountTransactionMapper;
import ru.ciklon.bank.bankcoreservice.core.mapper.ClientMapper;
import ru.ciklon.bank.bankcoreservice.core.mapper.CreditContractMapper;
import ru.ciklon.bank.bankcoreservice.core.mapper.CreditTransactionMapper;

@Configuration
public class BeanConfig {

    @Bean
    public AccountTransactionMapper accountTransactionMapper() {
        return Mappers.getMapper(AccountTransactionMapper.class);
    }

    @Bean
    public AccountMapper accountMapper() {
        return Mappers.getMapper(AccountMapper.class);
    }

    @Bean
    public CreditContractMapper creditContractMapper() {
        return Mappers.getMapper(CreditContractMapper.class);
    }

    @Bean
    public ClientMapper clientMapper() {
        return Mappers.getMapper(ClientMapper.class);
    }

    @Bean
    public CreditTransactionMapper creditTransactionMapper() {
        return Mappers.getMapper(CreditTransactionMapper.class);
    }
}
