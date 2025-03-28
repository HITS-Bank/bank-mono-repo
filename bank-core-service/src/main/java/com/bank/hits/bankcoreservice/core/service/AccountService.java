package com.bank.hits.bankcoreservice.core.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.*;
import com.bank.hits.bankcoreservice.api.enums.OperationType;
import com.bank.hits.bankcoreservice.api.enums.CreditTransactionType;
import com.bank.hits.bankcoreservice.core.entity.Account;
import com.bank.hits.bankcoreservice.core.entity.AccountTransaction;
import com.bank.hits.bankcoreservice.core.entity.Client;
import com.bank.hits.bankcoreservice.core.entity.CreditContract;
import com.bank.hits.bankcoreservice.core.entity.CreditTransaction;
import com.bank.hits.bankcoreservice.core.mapper.AccountMapper;
import com.bank.hits.bankcoreservice.core.mapper.AccountTransactionMapper;
import com.bank.hits.bankcoreservice.core.repository.AccountRepository;
import com.bank.hits.bankcoreservice.core.repository.ClientRepository;
import com.bank.hits.bankcoreservice.core.repository.CreditContractRepository;
import com.bank.hits.bankcoreservice.core.repository.AccountTransactionRepository;
import com.bank.hits.bankcoreservice.core.repository.CreditTransactionRepository;
import com.bank.hits.bankcoreservice.core.utils.AccountNumberGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@Slf4j
@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CreditContractRepository creditContractRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final ClientRepository clientRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountTransactionMapper accountTransactionMapper;
    private final AccountMapper accountMapper;


    @Transactional
    public AccountDto openAccount(final UUID clientId) {
        final String generatedAccountNumber = accountNumberGenerator.generateAccountNumber();
        final Client client = clientRepository.findByClientId(clientId)
                .orElseGet(() -> clientRepository.save(new Client(clientId)));
        Account account = new Account(client, generatedAccountNumber);
        account.setBalance(BigDecimal.ZERO);
        account = accountRepository.save(account);
        return accountMapper.map(account);
    }

    public void closeAccount(final CloseAccountRequest request) {
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.setClosed(true);
        accountRepository.save(account);
    }

    public AccountDto deposit(final TopUpRequest request) {
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.isClosed()) {
            throw new EntityNotFoundException("Account is closed");
        }
        if (account.isBlocked()) {
            throw new EntityNotFoundException("Account is blocked");
        }
        final var amount = new BigDecimal(request.getAmount());
        account.setBalance(account.getBalance().add(amount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        recordAccountTransaction(account, OperationType.TOP_UP, amount);
        return accountMapper.map(account);
    }

    public AccountDto withdraw(final WithdrawRequest request) {
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.isClosed()) {
            throw new EntityNotFoundException("Account is closed");
        }
        if (account.isBlocked()) {
            throw new EntityNotFoundException("Account is blocked");
        }
        final var amount = new BigDecimal(request.getAmount());

        account.setBalance(account.getBalance().subtract(amount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        recordAccountTransaction(account, OperationType.WITHDRAW, amount);
        return accountMapper.map(account);
    }

    public void blockAccount(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        final List<Account> accounts = accountRepository.findByClient(client);
        accounts.forEach(account -> account.setBlocked(true));
        accountRepository.saveAll(accounts);
    }

    public void unblockAccount(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        final List<Account> accounts = accountRepository.findByClient(client);
        accounts.forEach(account -> account.setBlocked(false));
        accountRepository.saveAll(accounts);
    }

    public List<AccountTransactionDto> getAccountHistory(final AccountNumberRequest request, final int pageSize, final int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("transactionDate")));
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return accountTransactionRepository.findByAccountId(account.getId(), pageable).stream()
                .map(accountTransactionMapper::map)
                .toList();
    }

    public List<AccountDto> getAccountsByClientId(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return accountRepository.findByClient(client).stream()
                .map(accountMapper::map)
                .toList();
    }

    public List<AccountTransactionDto> getAccountTransactionsByClientId(final UUID clientId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return accountRepository.findByClient(client).stream()
                .flatMap(account -> accountTransactionRepository.findByAccountId(account.getId()).stream())
                .sorted(Comparator.comparing(AccountTransaction::getTransactionDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(accountTransactionMapper::map)
                .toList();
    }


    /**
     * Частичное погашение кредита по запросу (например, от клиента).
     * Списываем указанную сумму с баланса счёта и уменьшаем остаток по кредитному договору.
     */
    public CreditPaymentResponseDTO repayCredit(final CreditRepaymentRequest repaymentRequest) {
        if (repaymentRequest.getCreditAmount() == null || new BigDecimal(repaymentRequest.getCreditAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }

        final CreditContract creditContract = creditContractRepository.findById(repaymentRequest.getCreditContractId())
                .orElseThrow(() -> new EntityNotFoundException("Credit contract not found"));

        final Account account = accountRepository.findById(creditContract.getAccount().getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        if (account.isClosed()) {
            throw new IllegalStateException("Account is closed");
        }

        final var amount = new BigDecimal(repaymentRequest.getCreditAmount());
        if (account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Insufficient funds in account for repayment");
        }


        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        recordCreditTransaction(creditContract, CreditTransactionType.CREDIT_REPAYMENT_AUTO, amount);

        creditContract.setRemainingAmount(creditContract.getRemainingAmount().max(BigDecimal.ZERO));
        creditContractRepository.save(creditContract);
        return new CreditPaymentResponseDTO(true, account.getBalance().toString());
    }

    private void recordAccountTransaction(final Account account, final OperationType type, final BigDecimal amount) {
        final AccountTransaction tx = new AccountTransaction();

        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setTransactionDate(LocalDateTime.now());

        accountTransactionRepository.save(tx);
    }

    private void recordCreditTransaction(final CreditContract creditContract, final CreditTransactionType type, final BigDecimal amount) {
        final CreditTransaction tx = new CreditTransaction();

        tx.setCreditContract(creditContract);
        tx.setTransactionType(type);
        tx.setPaymentAmount(amount);
        tx.setPaymentDate(LocalDateTime.now());

        creditTransactionRepository.save(tx);
    }

    public AccountDto getAccountById(final UUID accountId) {
        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        return accountMapper.map(account);
    }

    public AccountDto getAccountByAccountNumber(final AccountNumberRequest accountNumberRequest) {
        final Account account = accountRepository.findByAccountNumber(accountNumberRequest.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        return accountMapper.map(account);
    }

    public AccountsPaginationResponse getAllClientAccounts(final UUID clientId, int pageSize, int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("createdDate")));

        final Client client = clientRepository.findByClientId(clientId)
                .orElse(null);

        if (client == null) {
            clientRepository.save(new Client(clientId));
            return new AccountsPaginationResponse(
                    new PageInfo(pageSize, pageNumber + 1),
                    List.of()
            );
        }
        final Page<Account> accounts = accountRepository.findByClient(client, pageable);

        return new AccountsPaginationResponse(
                new PageInfo(pageSize, pageNumber + 1),
                accounts.stream().map(accountMapper::map).collect(Collectors.toList()));
    }

        private void validateTransfer(final Account source, final Account target, final String amountStr) {
        if (source.isClosed() || target.isClosed()) {
            throw new IllegalStateException("One of the accounts is closed");
        }
        if (source.isBlocked() || target.isBlocked()) {
            throw new IllegalStateException("One of the accounts is blocked");
        }

        final var amount = new BigDecimal(amountStr);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
        if (source.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient funds in source account");
        }
    }

    @Transactional
    public AccountDto transferBetweenOwnAccounts(final InternalTransferRequest request) {
        final Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        final Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Not enough balance for transfer");
        }

        validateTransfer(fromAccount, toAccount, String.valueOf(request.getAmount()));

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        recordAccountTransaction(fromAccount, OperationType.TRANSFER_OUT, request.getAmount());
        recordAccountTransaction(toAccount, OperationType.TRANSFER_IN, request.getAmount());

        return mapToDto(fromAccount);
    }

    @Transactional
    public AccountDto transferToAnotherClient(final ExternalTransferRequest request) {
        final Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        final Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        if (!toAccount.getClient().getClientId().equals(request.getToClientId())) {
            throw new IllegalArgumentException("Target account does not belong to the specified client");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Not enough balance for transfer");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        return mapToDto(fromAccount);
    }

    private AccountDto mapToDto(Account account) {
        AccountDto dto = new AccountDto();
        dto.setAccountId(account.getId());
        dto.setBalance(String.valueOf(account.getBalance()));
        dto.setAccountNumber(account.getAccountNumber());
        dto.setClosed(account.isClosed());
        dto.setBlocked(account.isBlocked());
        return dto;
    }
}
