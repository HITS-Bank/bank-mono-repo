package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;

import com.bank.hits.bankcoreservice.api.dto.AccountNumberRequest;
import com.bank.hits.bankcoreservice.api.dto.CloseAccountRequest;
import com.bank.hits.bankcoreservice.api.dto.ChangeBankAccountBalanceRequest;
import com.bank.hits.bankcoreservice.api.dto.TransferInfo;
import com.bank.hits.bankcoreservice.api.dto.TransferRequest;
import com.bank.hits.bankcoreservice.api.enums.CurrencyCode;
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
import com.bank.hits.bankcoreservice.api.dto.AccountDto;
import com.bank.hits.bankcoreservice.api.dto.AccountTransactionDto;
import com.bank.hits.bankcoreservice.core.service.AccountService;

import java.util.List;
import java.util.UUID;


@RequiredArgsConstructor
@RestController
@RequestMapping(ApiConstants.ACCOUNTS_BASE)
public class AccountController {

    private final AccountService accountService;

    @PostMapping(value = ApiConstants.CREATE_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> createAccount(@RequestHeader("userId") final UUID clientId, @RequestParam final CurrencyCode currencyCode) {
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
    public ResponseEntity<List<AccountDto>> getAccounts(@RequestHeader("userId") final UUID userId,
                                                                  @RequestParam final int pageSize,
                                                                  @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAllClientAccounts(userId, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.TOP_UP, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> top_up(@RequestHeader("userId") UUID clientId,
                                             @RequestParam final UUID accountId,
                                             @RequestBody final ChangeBankAccountBalanceRequest changeBankAccountBalanceRequest) {
        return ResponseEntity.ok(accountService.top_up(clientId, accountId, changeBankAccountBalanceRequest));
    }

    @PostMapping(value = ApiConstants.WITHDRAW, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> withdraw(@RequestHeader("userId") UUID clientId,
                                               @RequestParam final UUID accountId,
                                               @RequestBody final ChangeBankAccountBalanceRequest changeBankAccountBalanceRequest) {
        return ResponseEntity.ok(accountService.withdraw(clientId, accountId, changeBankAccountBalanceRequest));
    }

    @PostMapping(value = ApiConstants.ACCOUNT_HISTORY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountTransactionDto>> getAccountHistory(@RequestBody final AccountNumberRequest request, @RequestParam final int pageSize,
                                                                         @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAccountHistory(request, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.TRANSFER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> transferMoneyBetweenAccounts(final @RequestHeader("userId") UUID clientId,
                                                                   final @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.transferMoneyBetweenAccounts(clientId, request));
    }

    @PostMapping(value = ApiConstants.TRANSFER_INFO, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferInfo> getTransferInfo(final @RequestHeader("userId") UUID clientId,
                                                        final @RequestBody TransferRequest request) {
        return ResponseEntity.ok(accountService.getTransferInfo(clientId, request));
    }

}
