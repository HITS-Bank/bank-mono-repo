package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;
import com.bank.hits.bankcoreservice.api.dto.AccountHistoryPaginationResponse;
import com.bank.hits.bankcoreservice.api.dto.AccountsPaginationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.api.dto.AccountTransactionDto;
import com.bank.hits.bankcoreservice.api.dto.OpenAccountDto;
import com.bank.hits.bankcoreservice.api.dto.TransactionRequest;
import com.bank.hits.bankcoreservice.core.service.AccountService;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.ACCOUNTS_BASE)
public class AccountController {

    private final AccountService accountService;

    @PostMapping(ApiConstants.CREATE_ACCOUNT)
    public ResponseEntity<AccountDto> createAccount(@RequestBody final OpenAccountDto openAccountDto) {
        return ResponseEntity.ok(accountService.openAccount(openAccountDto));
    }

    @PostMapping(ApiConstants.CLOSE_ACCOUNT)
    public ResponseEntity<Void> closeAccount(@PathVariable final UUID accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ApiConstants.GET_ACCOUNT)
    public ResponseEntity<AccountDto> getAccount(@PathVariable final UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping(ApiConstants.GET_ACCOUNTS)
    public ResponseEntity<AccountsPaginationResponse> getAccounts(@PathVariable("userId") final UUID userId,
                                                                  @RequestParam final int pageSize,
                                                                  @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAllClientAccounts(userId, pageSize, pageNumber - 1));
    }

    @GetMapping(ApiConstants.GET_ACCOUNT_BY_ACCOUNT_NUMBER)
    public ResponseEntity<AccountDto> getAccount(@RequestParam final String accountNumber) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumber));
    }

    @PostMapping(ApiConstants.DEPOSIT)
    public ResponseEntity<AccountDto> deposit(@RequestHeader("userId") UUID clientId, @RequestBody final TransactionRequest transactionRequest) {
        return ResponseEntity.ok(accountService.deposit(transactionRequest));
    }

    @PostMapping(ApiConstants.WITHDRAW)
    public ResponseEntity<AccountDto> withdraw(@RequestHeader("userId") UUID clientId, @RequestBody final TransactionRequest transactionRequest) {
        return ResponseEntity.ok(accountService.withdraw(transactionRequest));
    }

    @PostMapping(ApiConstants.ACCOUNT_HISTORY)
    public ResponseEntity<AccountHistoryPaginationResponse> getAccountHistory(@PathVariable final UUID accountId, @RequestParam final int pageSize,
                                                                              @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAccountHistory(accountId, pageSize, pageNumber - 1));
    }

}
