package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.dto.*;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.repository.AccountRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.mapper.ClientMapper;
import com.bank.hits.bankcoreservice.core.repository.ClientRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClientService {
    private final AccountRepository accountRepository;

    private final ClientRepository clientRepository;
    private final AccountService accountService;
    private final CreditService creditService;
    private final EmployeeService employeeService;
    private final ClientMapper clientMapper;

    public ClientDto createClient(final ClientDto clientDto) {
        return clientMapper.map(clientRepository.save(new Client(clientDto.getClientId())));
    }

    public List<AccountDto> getAccountsList(final UUID clientId,
                                         final UUID employeeId,
                                         final int pageSize,
                                         final int pageNumber) { // pageNumber - 1
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        if (employeeService.isEmployeeBlocked(employeeId)) {
            throw new RuntimeException("Employee is blocked");
        }

        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("createdDate")));

        return accountService.getAccountsByClientId(clientId, pageable);
    }


    public ClientInfoDto getClientInfoForCredit(final UUID clientId) {
        final ClientInfoDto clientInfoDto = new ClientInfoDto();

        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        clientInfoDto.setClientId(clientId);

        log.info("getClientInfoForCredit - {}", clientInfoDto);
        log.info("client - {}", client);
        final List<AccountDto> accountDtos = accountService.getAccountsByClientId(clientId);
        clientInfoDto.setAccounts(accountDtos);

        final List<AccountTransactionDto> accountTransactionDtos = accountService.getAccountTransactionsByClientId(clientId);
        clientInfoDto.setAccountTransactions(accountTransactionDtos);

        final List<CreditContractDto> creditContractDtos = creditService.getCreditsByClientId(clientId);
        clientInfoDto.setCredits(creditContractDtos);

        final List<CreditTransactionDto> creditContractTransactionDtos = creditService.getCreditContractTransactionsByClientId(clientId);
        clientInfoDto.setCreditTransactions(creditContractTransactionDtos);

        clientInfoDto.setCreditRating(client.getCreditRating());
        Optional<Account> masterAcc = accountRepository.findByAccountNumber("MASTER-0000000001");
        clientInfoDto.setMasterAccountAmount(masterAcc.get().getBalance());
        log.info("getClientInfoForCredit - {}", clientInfoDto);

        return clientInfoDto;
    }

    public CreditRatingResponseDTO getCreditRating(UUID clientId)
    {
        Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Клиент не найден: " + clientId));
        return new CreditRatingResponseDTO(client.getCreditRating());
    }

    public void blockClientAccounts(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        clientRepository.save(client.withBlocked(true));
        accountService.blockAccount(clientId);
    }

    public void unblockClientAccounts(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        clientRepository.save(client.withBlocked(false));
        accountService.unblockAccount(clientId);
    }
}
