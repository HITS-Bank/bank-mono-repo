package com.bank.hits.bankcoreservice.core.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ciklon.bank.bankcoreservice.api.dto.AccountDto;
import ru.ciklon.bank.bankcoreservice.api.dto.AccountTransactionDto;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientDto;
import ru.ciklon.bank.bankcoreservice.api.dto.ClientInfoDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditContractDto;
import ru.ciklon.bank.bankcoreservice.api.dto.CreditTransactionDto;
import ru.ciklon.bank.bankcoreservice.core.entity.Client;
import ru.ciklon.bank.bankcoreservice.core.mapper.ClientMapper;
import ru.ciklon.bank.bankcoreservice.core.repository.ClientRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final CreditService creditService;
    private final EmployeeService employeeService;
    private final ClientMapper clientMapper;

    public ClientDto createClient(ClientDto clientDto) {
        return clientMapper.map(clientRepository.save(new Client(clientDto.getClientId(), false)));
    }

    public ClientInfoDto getClientInfo(final UUID clientId, final UUID employeeId) {
        final ClientInfoDto clientInfoDto = new ClientInfoDto();

        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        clientInfoDto.setClientId(client.getId());

        if (employeeService.isEmployeeBlocked(employeeId)) {
            throw new RuntimeException("Employee is blocked");
        }

        final List<AccountDto> accountDtos = accountService.getAccountsByClientId(clientId);
        clientInfoDto.setAccounts(accountDtos);

        final List<AccountTransactionDto> accountTransactionDtos = accountService.getAccountTransactionsByClientId(clientId);
        clientInfoDto.setAccountTransactions(accountTransactionDtos);

        final List<CreditContractDto> creditContractDtos = creditService.getCreditsByClientId(clientId);
        clientInfoDto.setCredits(creditContractDtos);

        final List<CreditTransactionDto> creditContractTransactionDtos = creditService.getCreditContractTransactionsByClientId(clientId);
        clientInfoDto.setCreditTransactions(creditContractTransactionDtos);

        return clientInfoDto;
    }


    public ClientInfoDto getClientInfoForCredit(final UUID clientId) {
        final ClientInfoDto clientInfoDto = new ClientInfoDto();

        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        clientInfoDto.setClientId(client.getId());

        final List<AccountDto> accountDtos = accountService.getAccountsByClientId(clientId);
        clientInfoDto.setAccounts(accountDtos);

        final List<AccountTransactionDto> accountTransactionDtos = accountService.getAccountTransactionsByClientId(clientId);
        clientInfoDto.setAccountTransactions(accountTransactionDtos);

        final List<CreditContractDto> creditContractDtos = creditService.getCreditsByClientId(clientId);
        clientInfoDto.setCredits(creditContractDtos);

        final List<CreditTransactionDto> creditContractTransactionDtos = creditService.getCreditContractTransactionsByClientId(clientId);
        clientInfoDto.setCreditTransactions(creditContractTransactionDtos);

        return clientInfoDto;
    }


    public void blockClientAccounts(final UUID clientId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        clientRepository.save(client.withBlocked(true));
        accountService.blockAccount(clientId);
    }

    public void unblockClientAccounts(final UUID clientId) {
        final Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        clientRepository.save(client.withBlocked(false));
        accountService.unblockAccount(clientId);
    }
}
