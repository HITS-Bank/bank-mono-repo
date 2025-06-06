package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.enums.AccountType;
import com.bank.hits.bankcoreservice.config.service.KafkaProducerService;
import com.bank.hits.bankcoreservice.config.websocket.TransactionWebSocketController;
import com.bank.hits.bankcoreservice.core.entity.*;
import com.bank.hits.bankcoreservice.core.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import com.bank.hits.bankcoreservice.api.dto.*;
import com.bank.hits.bankcoreservice.api.enums.OperationType;
import com.bank.hits.bankcoreservice.api.enums.CreditTransactionType;
import com.bank.hits.bankcoreservice.core.mapper.AccountMapper;
import com.bank.hits.bankcoreservice.core.mapper.AccountTransactionMapper;
import com.bank.hits.bankcoreservice.core.utils.AccountNumberGenerator;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@AllArgsConstructor
public class AccountService {

    private final CreditRatingService creditRatingService;
    private final AccountRepository accountRepository;
    private final CreditContractRepository creditContractRepository;
    private final AccountTransactionRepository accountTransactionRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final ClientRepository clientRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final AccountTransactionMapper accountTransactionMapper;

    private static final String MASTER_ACCOUNT_NUMBER = "00000000000000000001";
    private final AccountMapper accountMapper;
    private final OverduePaymentRepository overduePaymentRepository;

    private final KafkaProducerService kafkaProducerService;

    @Autowired
    private CurrencyConversionService conversionService;

    @Autowired
    private TransactionWebSocketController transactionWebSocketController;

    @Transactional
    public AccountDto openAccount(final UUID clientId, final CurrencyCode currencyCode) {
        final String generatedAccountNumber = accountNumberGenerator.generateAccountNumber();
        final Client client = clientRepository.findByClientId(clientId)
                .orElseGet(() -> clientRepository.save(new Client(clientId)));
        Account account = new Account(client, generatedAccountNumber, currencyCode);
        account.setBalance(BigDecimal.ZERO);
        account.setAccountType(AccountType.CHECKING);
        account = accountRepository.save(account);
        return accountMapper.map(account);
    }

    public void closeAccount(final CloseAccountRequest request) {
        final Account account = accountRepository.findById(UUID.fromString(request.getAccountId()))
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.setClosed(true);
        accountRepository.save(account);
    }

    public void closeAccount(final UUID accountId) {
        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.setClosed(true);
        accountRepository.save(account);
    }
    /*
    public AccountDto deposit(final TopUpRequest request, String accountId) {
        final Account account = accountRepository.findById(UUID.fromString(accountId))
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.isClosed()) {
            throw new EntityNotFoundException("Account is closed");
        }
        if (account.isBlocked()) {
            throw new EntityNotFoundException("Account is blocked");
        }
    }


     */

    public AccountDto top_up(final UUID clientId, final UUID accountId, final ChangeBankAccountBalanceRequest request) {
        final Account account = validateInputDataAndReturnAccount(clientId, accountId);

        final var inputCurrency = request.getCurrencyCode();
        final var accountCurrency = account.getCurrencyCode();
        final var amount = new BigDecimal(request.getAmount());

        BigDecimal convertedAmount;
        if(inputCurrency != accountCurrency) {
            try {
                log.info("inputCurrency: {}", inputCurrency);
                log.info("accountCurrency: {}", accountCurrency);
                log.info("amount: {}", amount);
                convertedAmount = conversionService.convert(inputCurrency, accountCurrency, amount);
            } catch (Exception ex) {
                throw new RuntimeException("Error during currency conversion");
            }
        }
        else convertedAmount = amount;

        account.setBalance(account.getBalance().add(convertedAmount));
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        final var accountTransactionDto = recordAccountTransaction(account, OperationType.TOP_UP, amount, inputCurrency);

        transactionWebSocketController.sendTransactionUpdate(accountId, accountTransactionDto);

        kafkaProducerService.sendOperationEvent(map(accountTransactionDto, clientId, account.getBalance()));
        return accountMapper.map(account);
    }

    private AccountOperationEvent map(final AccountTransactionDto transactionDto, final UUID userId, final BigDecimal accountBalance) {
        return AccountOperationEvent.builder()
                .accountBalance(accountBalance)
                .operationType(transactionDto.getType())
                .userId(String.valueOf(userId))
                .operationAmount(new BigDecimal(transactionDto.getAmount()))
                .currencyCode(transactionDto.getCurrencyCode())
                .build();
    }

    public AccountDto withdraw(final WithdrawRequest request, final UUID accountId) {
        final Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        if (account.isClosed()) {
            throw new EntityNotFoundException("Account is closed");
        }
        if (account.isBlocked()) {
            throw new EntityNotFoundException("Account is blocked");
        }

        final var inputCurrency = request.getCurrencyCode();
        final var accountCurrency = account.getCurrencyCode();
        final var amount = new BigDecimal(request.getAmount());
        BigDecimal convertedAmount;
        if(inputCurrency != accountCurrency) {
            try {
                log.info("inputCurrency: {}", inputCurrency);
                log.info("accountCurrency: {}", accountCurrency);
                log.info("amount: {}", amount);
                convertedAmount = conversionService.convert(inputCurrency, accountCurrency, amount);
            } catch (Exception ex) {
                throw new RuntimeException("Error during currency conversion");
            }
        }
        else {
            convertedAmount = amount;
        }

        account.setBalance(account.getBalance().subtract(convertedAmount));

        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new EntityNotFoundException("Insufficient funds");
        }
        accountRepository.save(account);
        final var accountTransactionDto = recordAccountTransaction(account, OperationType.WITHDRAW, amount, inputCurrency);

        transactionWebSocketController.sendTransactionUpdate(accountId, accountTransactionDto);
        kafkaProducerService.sendOperationEvent(map(accountTransactionDto, account.getClient().getClientId(), account.getBalance()));

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

    public List<AccountTransactionDto> getAccountHistory(final UUID accountId, final int pageSize, final int pageNumber) {
        final Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Order.desc("transactionDate")));
        final Account account = accountRepository.findById(accountId)
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
        log.info("Зашли в оплату");
        if (repaymentRequest.getCreditAmount() == null || new BigDecimal(repaymentRequest.getCreditAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }

        final CreditContract creditContract = creditContractRepository.findByCreditApprovedId(repaymentRequest.getCreditContractId())
                .orElseThrow(() -> new EntityNotFoundException("Credit contract not found"));

        log.info("creditContract: {}", creditContract);
        final Account account = accountRepository.findById(creditContract.getAccount().getId())
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        log.info("account: {}", account);
        if (account.isClosed()) {
            throw new IllegalStateException("Account is closed");
        }

        log.info("Над amount");
        var amount = new BigDecimal(repaymentRequest.getCreditAmount());
        if (amount.compareTo(repaymentRequest.getRemainingAmount()) > 0) {
            amount = repaymentRequest.getRemainingAmount();
        }
        if (account.getBalance().subtract(amount).compareTo(BigDecimal.ZERO) < 0) {
            log.info("Просрочен, начало обработки");
            OverduePayment overduePayment = new OverduePayment();
            overduePayment.setCreditContractId(creditContract.getCreditContractId());
            overduePayment.setPaymentAmount(amount);
            overduePayment.setPaymentDate(LocalDateTime.now());
            overduePaymentRepository.save(overduePayment);
            log.info("Просрочен, обработано");
            return new CreditPaymentResponseDTO(false, account.getBalance());
        }

        log.info("Можем оплатить:");


        Account masterAccount = accountRepository.findByAccountNumber(MASTER_ACCOUNT_NUMBER)
                .orElseThrow(() -> new IllegalStateException("Master account not found"));

        masterAccount.setBalance(masterAccount.getBalance().add(amount));
        accountRepository.save(masterAccount);

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        recordCreditTransaction(creditContract, CreditTransactionType.CREDIT_REPAYMENT_AUTO, amount, repaymentRequest.getPaymentStatus());
        var transaction = recordAccountTransaction(account, OperationType.LOAN_PAYMENT, amount, CurrencyCode.RUB);

        kafkaProducerService.sendOperationEvent(map(transaction, account.getClient().getClientId(), account.getBalance()));

        transactionWebSocketController.sendTransactionUpdate(account.getId(), transaction);

        creditContract.setRemainingAmount(creditContract.getRemainingAmount().subtract(amount).max(BigDecimal.ZERO));
        creditContractRepository.save(creditContract);
        log.info("Credit contract remaining amount {}", creditContract.getRemainingAmount());

        if (creditContract.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            log.info("Зашли в изменение кредитного рейтинга");
            Client client = account.getClient();
            creditRatingService.updateCreditRating(client, creditContract);
        }
        return new CreditPaymentResponseDTO(true, amount);
    }

    private AccountTransactionDto recordAccountTransaction(final Account account, final OperationType type, final BigDecimal amount, final CurrencyCode currencyCode) {
        final AccountTransaction tx = new AccountTransaction();

        tx.setAccount(account);
        tx.setTransactionType(type);
        tx.setAmount(amount);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setCurrencyCode(currencyCode);

        return accountTransactionMapper.map(accountTransactionRepository.save(tx));
    }

    private void recordCreditTransaction(final CreditContract creditContract, final CreditTransactionType type, final BigDecimal amount, final PaymentStatus paymentStatus) {
        final CreditTransaction tx = new CreditTransaction();

        tx.setCreditContract(creditContract);
        tx.setCreditContractId(creditContract.getCreditContractId());
        tx.setTransactionType(type);
        tx.setPaymentAmount(amount);
        tx.setPaymentDate(LocalDateTime.now());
        tx.setPaymentStatus(paymentStatus);

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

    private void validateTransfer(final Account source, final Account target, final String amountStr) {

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
        final Account toAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
                .orElseThrow(() -> new RuntimeException("Recipient account not found"));

        final BigDecimal transferAmount = new BigDecimal(request.getTransferAmount());

        validateTransfer(fromAccount, toAccount, request.getTransferAmount());

        final CurrencyCode fromCurrency = fromAccount.getCurrencyCode();
        final CurrencyCode toCurrency = toAccount.getCurrencyCode();
        final BigDecimal originalAmount = new BigDecimal(request.getTransferAmount());
        BigDecimal convertedAmount;
        if(fromCurrency != toCurrency)
        {
            try{
                convertedAmount = conversionService.convert(fromCurrency, toCurrency, originalAmount);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error during currency conversion");
            }
        } else {
            convertedAmount = originalAmount;
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(originalAmount));
        toAccount.setBalance(toAccount.getBalance().add(convertedAmount));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        final var fromTransaction = recordAccountTransaction(fromAccount, OperationType.TRANSFER_OUTGOING, transferAmount, fromCurrency);
        final var toTransaction = recordAccountTransaction(toAccount, OperationType.TRANSFER_INCOMING, convertedAmount, toCurrency);
        kafkaProducerService.sendOperationEvent(map(fromTransaction, fromAccount.getClient().getClientId(), fromAccount.getBalance()));
        kafkaProducerService.sendOperationEvent(map(toTransaction, toAccount.getClient().getClientId(), toAccount.getBalance()));


        transactionWebSocketController.sendTransactionUpdate(fromAccount.getId(), fromTransaction);
        transactionWebSocketController.sendTransactionUpdate(toAccount.getId(), toTransaction);

        return mapToDto(fromAccount);
    }

    public TransferInfo getTransferInfo(final UUID clientId, final TransferRequest request) {
        final Client client = clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Client not found"));

        if (client.isBlocked()) {
            throw new EntityNotFoundException("Client is blocked");
        }

        final Account fromAccount = accountRepository.findById(request.getSenderAccountId())
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        final Account toAccount = accountRepository.findByAccountNumber(request.getReceiverAccountNumber())
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

        final CurrencyCode fromCurrency = fromAccount.getCurrencyCode();
        final CurrencyCode toCurrency = toAccount.getCurrencyCode();
        final BigDecimal originalAmount = new BigDecimal(request.getTransferAmount());
        BigDecimal convertedAmount = null;
        if(fromCurrency != toCurrency)
        {
            try{
                log.info("fromCurrency = " + fromCurrency.toString());
                log.info("toCurrency = " + toCurrency.toString());
                log.info("originalAmount = " + originalAmount);
                convertedAmount = conversionService.convert(fromCurrency, toCurrency, originalAmount);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error during currency conversion");
            }
        } else {
            convertedAmount = originalAmount;
        }

        transferInfo.setTransferAmountAfterConversion(String.valueOf(convertedAmount));

        return transferInfo;
    }

    private AccountDto mapToDto(final Account account) {
        final AccountDto dto = new AccountDto();
        dto.setAccountId(account.getId());
        dto.setBalance(String.valueOf(account.getBalance()));
        dto.setAccountNumber(account.getAccountNumber());
        dto.setClosed(account.isClosed());
        dto.setBlocked(account.isBlocked());
        dto.setCurrencyCode(account.getCurrencyCode().toString());
        return dto;
    }

    public List<PaymentResponseDTO> getPayments(UUID loanId)
    {
        List<PaymentResponseDTO> result = new ArrayList<>();
        CreditContract creditContract = creditContractRepository.findByCreditApprovedId(loanId)
                .orElseThrow(() -> new RuntimeException("Credit contract not found"));
        List<CreditTransaction> successfulPayments = creditTransactionRepository.findByCreditContract(creditContract);
        log.info("SuccessfulPayments: {}", successfulPayments);
        for (CreditTransaction tx : successfulPayments) {
            PaymentResponseDTO dto = new PaymentResponseDTO();
            dto.setId(tx.getTransactionId());
            dto.setStatus(tx.getPaymentStatus() != null ? tx.getPaymentStatus().toString() : null);
            dto.setDateTime(tx.getPaymentDate());
            dto.setAmount(tx.getPaymentAmount());
            dto.setCurrencyCode("RUB");
            result.add(dto);
        }
        List<OverduePayment> overduePayments = overduePaymentRepository.findByCreditContractId(creditContract.getCreditContractId());
        for (OverduePayment op : overduePayments) {
            PaymentResponseDTO dto = new PaymentResponseDTO();
            dto.setId(op.getId());
            dto.setStatus("OVERDUE");
            dto.setDateTime(op.getPaymentDate());
            dto.setAmount(op.getPaymentAmount());
            dto.setCurrencyCode("RUB");
            result.add(dto);
        }
        return result;
    }
}

