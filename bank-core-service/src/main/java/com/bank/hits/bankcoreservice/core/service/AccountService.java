package com.bank.hits.bankcoreservice.core.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.*;
import com.bank.hits.bankcoreservice.api.enums.AccountTransactionType;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
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


    public AccountDto openAccount(final OpenAccountDto openAccountDto) {
        final String generatedAccountNumber = accountNumberGenerator.generateAccountNumber();
        final Client client = clientRepository.findById(openAccountDto.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));
        final Account account = new Account(client, generatedAccountNumber);
        accountRepository.save(account);
        return accountMapper.map(account);
    }

    public void closeAccount(final UUID accountId) {
        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        account.setClosed(true);
        accountRepository.save(account);
    }

    public AccountDto deposit(final TransactionRequest request) {
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.isClosed()) {
            throw new RuntimeException("Account is closed");
        }
        final var amount = new BigDecimal(request.getAmount());
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        recordAccountTransaction(account, AccountTransactionType.DEPOSIT, amount);
        return accountMapper.map(account);
    }

    public AccountDto withdraw(final TransactionRequest request) {
        final Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if (account.isClosed()) {
            throw new RuntimeException("Account is closed");
        }
        final var amount = new BigDecimal(request.getAmount());

        account.setBalance(account.getBalance().subtract(amount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient funds");
        }
        accountRepository.save(account);
        recordAccountTransaction(account, AccountTransactionType.WITHDRAW, amount);
        return accountMapper.map(account);
    }

    public void blockAccount(final UUID clientId) {
        final List<Account> accounts = accountRepository.findByClientId(clientId);
        accounts.forEach(account -> account.setBlocked(true));
        accountRepository.saveAll(accounts);
    }

    public void unblockAccount(final UUID clientId) {
        final List<Account> accounts = accountRepository.findByClientId(clientId);
        accounts.forEach(account -> account.setBlocked(false));
        accountRepository.saveAll(accounts);
    }

    public AccountHistoryPaginationResponse getAccountHistory(final UUID accountId, final int pageSize, final int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return new AccountHistoryPaginationResponse(
                new PageInfo(pageSize, pageNumber + 1),
                accountTransactionRepository.findByAccountId(accountId, pageable).stream()
                        .map(accountTransactionMapper::map)
                        .toList());
    }

    public List<AccountDto> getAccountsByClientId(final UUID clientId) {
        return accountRepository.findByClientId(clientId).stream()
                .map(accountMapper::map)
                .toList();
    }

    public List<AccountTransactionDto> getAccountTransactionsByClientId(final UUID clientId) {
        return accountRepository.findByClientId(clientId).stream()
                .flatMap(account -> accountTransactionRepository.findByAccountId(account.getId()).stream())
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
                .orElseThrow(() -> new RuntimeException("Credit contract not found"));

        final Account account = accountRepository.findById(creditContract.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

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
        return null;
    }

    private void recordAccountTransaction(final Account account, final AccountTransactionType type, final BigDecimal amount) {
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
                .orElseThrow(() -> new RuntimeException("Account not found"));
        return accountMapper.map(account);
    }

    public AccountDto getAccountByAccountNumber(final String accountNumber) {
        final Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return accountMapper.map(account);
    }

    public AccountsPaginationResponse getAllClientAccounts(final UUID clientId, int pageSize, int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize);

        final Page<Account> accounts = accountRepository.findByClientId(clientId, pageable);

        return new AccountsPaginationResponse(
                new PageInfo(pageSize, pageNumber + 1),
                accounts.stream().map(accountMapper::map).collect(Collectors.toList()));
    }
}
