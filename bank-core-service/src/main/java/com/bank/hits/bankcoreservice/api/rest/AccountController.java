package com.bank.hits.bankcoreservice.api.rest;

import com.bank.hits.bankcoreservice.api.constant.ApiConstants;

import com.bank.hits.bankcoreservice.api.dto.*;
import com.bank.hits.bankcoreservice.config.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    private final JwtUtils jwtUtils;

    @PostMapping(value = ApiConstants.CREATE_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> createAccount(@RequestParam CurrencyCode currencyCode,
                                                    HttpServletRequest httpServletRequest) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.openAccount(clientId, currencyCode));
    }

    @PostMapping(value = ApiConstants.CLOSE_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> closeAccount(@PathVariable("accountId") final UUID accountId,
                                             HttpServletRequest httpServletRequest) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        accountService.closeAccount(accountId, clientId);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = ApiConstants.GET_ACCOUNT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> getAccount(@PathVariable final UUID accountId) {
        return ResponseEntity.ok(accountService.getAccountById(accountId));
    }

    @GetMapping(value = ApiConstants.GET_ACCOUNTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountDto>> getAccounts(HttpServletRequest httpServletRequest,
                                                                  @RequestParam final int pageSize,
                                                                  @RequestParam final int pageNumber) {
        final UUID userId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.getAllClientAccounts(userId, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.TOP_UP, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> top_up(HttpServletRequest httpServletRequest,
                                             @PathVariable final UUID accountId,
                                             @RequestBody final ChangeBankAccountBalanceRequest changeBankAccountBalanceRequest) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.top_up(clientId, accountId, changeBankAccountBalanceRequest));
    }

    @PostMapping(value = ApiConstants.WITHDRAW, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> withdraw(HttpServletRequest httpServletRequest,
                                               @PathVariable final UUID accountId,
                                               @RequestBody final WithdrawRequest withdrawRequest) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.withdraw(withdrawRequest, accountId));
    }

    @PostMapping(value = ApiConstants.ACCOUNT_HISTORY, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AccountTransactionDto>> getAccountHistory(@PathVariable("accountId") final UUID accountId,
                                                                         @RequestParam final int pageSize,
                                                                         @RequestParam final int pageNumber) {
        return ResponseEntity.ok(accountService.getAccountHistory(accountId, pageSize, pageNumber - 1));
    }

    @PostMapping(value = ApiConstants.TRANSFER, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AccountDto> transferMoneyBetweenAccounts(HttpServletRequest httpServletRequest,
                                                                   final @RequestBody TransferRequest request) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.transferMoneyBetweenAccounts(clientId, request));
    }

    @PostMapping(value = ApiConstants.TRANSFER_INFO, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TransferInfo> getTransferInfo(HttpServletRequest httpServletRequest,
                                                        final @RequestBody TransferRequest request) {
        final UUID clientId = UUID.fromString(jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest)));
        return ResponseEntity.ok(accountService.getTransferInfo(clientId, request));
    }

    @GetMapping(value = ApiConstants.LOAN_PAYMENTS, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<PaymentResponseDTO> getPayments(@PathVariable("loanId") UUID loanId) {
        return ResponseEntity.ok(accountService.getPayments(loanId)).getBody();
    }

}
