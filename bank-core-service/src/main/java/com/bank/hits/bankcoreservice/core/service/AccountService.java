package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
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
    public AccountDto openAccount(final UUID clientId, final CurrencyCode currencyCode) {
        final String generatedAccountNumber = accountNumberGenerator.generateAccountNumber();
        final Client client = clientRepository.findByClientId(clientId)
                .orElseGet(() -> clientRepository.save(new Client(clientId)));
        Account account = new Account(client, generatedAccountNumber, currencyCode);
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



    public AccountDto top_up(final UUID clientId, final UUID accountId, final ChangeBankAccountBalanceRequest request) {
        final Account account = validateInputDataAndReturnAccount(clientId, accountId);

        final var amount = new BigDecimal(request.getAmount());
        account.setBalance(account.getBalance().add(amount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        recordAccountTransaction(account, OperationType.TOP_UP, amount);
        return accountMapper.map(account);
    }

    public AccountDto withdraw(final UUID clientId, final UUID accountId, final ChangeBankAccountBalanceRequest request) {
        final Account account = validateInputDataAndReturnAccount(clientId, accountId);

        final var amount = new BigDecimal(request.getAmount());
        account.setBalance(account.getBalance().subtract(amount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        recordAccountTransaction(account, OperationType.WITHDRAW, amount);
        return accountMapper.map(account);
    }

    private Account validateInputDataAndReturnAccount(final UUID clientId, final UUID accountId) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        if (client.isBlocked()) {
            throw new EntityNotFoundException("Client is blocked");
        }

        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (!account.getClient().equals(client)) {
            throw new EntityNotFoundException("Account does not belong to the specified client");
        }
        if (account.isClosed()) {
            throw new EntityNotFoundException("Account is closed");
        }
        if (account.isBlocked()) {
            throw new EntityNotFoundException("Account is blocked");
        }

        return account;
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

    public List<AccountDto> getAccountsByClientId(final UUID clientId, final Pageable pageable) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));
        return accountRepository.findByClient(client, pageable).stream()
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

    public List<AccountDto> getAllClientAccounts(final UUID clientId, final int pageSize, final int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("createdDate")));

        final Client client = clientRepository.findByClientId(clientId)
                .orElse(null);

        if (client == null) {
            clientRepository.save(new Client(clientId));
            return List.of();
        }
        final Page<Account> accounts = accountRepository.findByClient(client, pageable);

        return accounts.stream().map(accountMapper::map).toList();
    }

    private void validateTransfer(final Client client, final Account source, final Account target, final String amountStr) {
        if (!client.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Account does not belong to the specified client");
        }

        if (source.isClosed() || target.isClosed()) {
            throw new IllegalStateException("One of the accounts is closed");
        }
        if (source.isBlocked() || target.isBlocked()) {
            throw new IllegalStateException("One of the accounts is blocked");
        }

        final var amount = new BigDecimal(amountStr);

        if (source.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Not enough balance for transfer");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }
    }

    @Transactional
    public AccountDto transferMoneyBetweenAccounts(final UUID clientId, final TransferRequest request) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        if (client.isBlocked()) {
            throw new EntityNotFoundException("Client is blocked");
        }

        final Account fromAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));
        final Account toAccount = accountRepository.findById(request.getReceiverAccountId())
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        final BigDecimal transferAmount = new BigDecimal(request.getTransferAmount());

        validateTransfer(client, fromAccount, toAccount, request.getTransferAmount());

        fromAccount.setBalance(fromAccount.getBalance().subtract(transferAmount));
        toAccount.setBalance(toAccount.getBalance().add(transferAmount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        recordAccountTransaction(fromAccount, OperationType.TRANSFER_OUTGOING, transferAmount);
        recordAccountTransaction(toAccount, OperationType.TRANSFER_INCOMING, transferAmount);

        return mapToDto(fromAccount);
    }

    private AccountDto mapToDto(final Account account) {
        final AccountDto dto = new AccountDto();
        dto.setAccountId(account.getId());
        dto.setBalance(String.valueOf(account.getBalance()));
        dto.setAccountNumber(account.getAccountNumber());
        dto.setClosed(account.isClosed());
        dto.setBlocked(account.isBlocked());
        return dto;
    }

    public TransferInfo getTransferInfo(final UUID clientId, final TransferRequest request) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        if (client.isBlocked()) {
            throw new EntityNotFoundException("Client is blocked");
        }

        final Account fromAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        final Account toAccount = accountRepository.findById(request.getReceiverAccountId())
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        final TransferInfo transferInfo = new TransferInfo();

        final TransferAccountInfo fromAccountInfo = new TransferAccountInfo();
        fromAccountInfo.setAccountId(fromAccount.getId());
        fromAccountInfo.setAccountNumber(fromAccount.getAccountNumber());
        fromAccountInfo.setAccountCurrencyCode(fromAccount.getCurrencyCode());

        final TransferAccountInfo toAccountInfo = new TransferAccountInfo();
        toAccountInfo.setAccountId(toAccount.getId());
        toAccountInfo.setAccountNumber(toAccount.getAccountNumber());
        toAccountInfo.setAccountCurrencyCode(toAccount.getCurrencyCode());

        transferInfo.setSenderAccountInfo(fromAccountInfo);
        transferInfo.setReceiverAccountInfo(toAccountInfo);
        transferInfo.setTransferAmountBeforeConversion(request.getTransferAmount());

        // TODO: add currency conversion logic here

        return transferInfo;
    }
}
