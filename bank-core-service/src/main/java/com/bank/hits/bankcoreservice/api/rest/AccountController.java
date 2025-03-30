package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;

import com.bank.hits.bankcoreservice.api.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.bank.hits.bankcoreservice.core.service.AccountService;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.ACCOUNTS_BASE)
public class AccountController {

    private final AccountService accountService;

    @PostMapping(value = ApiConstants.CREATE_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> createAccount(@RequestHeader("userId") final UUID clientId, @RequestParam CurrencyCode currencyCode) {
        return ResponseEntity.ok(accountService.openAccount(clientId, currencyCode));
    }

    @PostMapping(value = ApiConstants.CLOSE_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> closeAccount(@RequestBody final CloseAccountRequest request) {
        accountService.closeAccount(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = ApiConstants.GET_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> getAccount(@PathVariable final UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping(value = ApiConstants.GET_ACCOUNTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountsPaginationResponse> getAccounts(@RequestHeader("userId") final UUID userId,
                                                                  @RequestParam final int pageSize,
                                                                  @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAllClientAccounts(userId, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.GET_ACCOUNT_BY_ACCOUNT_NUMBER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> getAccount(@RequestBody final AccountNumberRequest accountNumberRequest) {
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumberRequest));
    }

    @PostMapping(value = ApiConstants.DEPOSIT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> deposit(@RequestHeader("userId") UUID clientId, @RequestBody final TopUpRequest transactionRequest, @RequestParam String accountId) {
        return ResponseEntity.ok(accountService.deposit(transactionRequest, accountId));
    }

    @PostMapping(value = ApiConstants.WITHDRAW, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> withdraw(@RequestHeader("userId") UUID clientId, @RequestBody final WithdrawRequest transactionRequest, @RequestParam String accountId) {
        return ResponseEntity.ok(accountService.withdraw(transactionRequest, accountId));
    }

    @PostMapping(value = ApiConstants.ACCOUNT_HISTORY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountTransactionDto>> getAccountHistory(@RequestBody final AccountNumberRequest request, @RequestParam final int pageSize,
                                                                         @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAccountHistory(request, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.TRANSFER_INTERNAL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> transferBetweenOwnAccounts(final @RequestHeader("userId") UUID clientId,
                                                                 final @RequestBody InternalTransferRequest request) {
        return ResponseEntity.ok(accountService.transferBetweenOwnAccounts(request));
    }

    @PostMapping(value = ApiConstants.TRANSFER_EXTERNAL, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> transferToAnotherClient(final @RequestHeader("userId") UUID clientId,
                                                              final @RequestBody ExternalTransferRequest request) {
        return ResponseEntity.ok(accountService.transferToAnotherClient(request));
    }

    @GetMapping("/loan/{loanId}/payments")
    public List<PaymentResponseDTO> getPayments(@PathVariable("loanId") UUID loanId) {
        return ResponseEntity.ok(accountService.getPayments(loanId)).getBody();
    }

}
