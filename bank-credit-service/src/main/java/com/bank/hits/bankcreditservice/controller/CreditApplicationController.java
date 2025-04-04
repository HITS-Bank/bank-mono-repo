package com.bank.hits.bankcreditservice.controller;

import com.bank.hits.bankcreditservice.exception.ForbiddenAccessException;
import com.bank.hits.bankcreditservice.model.DTO.*;
import com.bank.hits.bankcreditservice.service.api.CreditApplicationService;
import com.bank.hits.bankcreditservice.service.api.CreditPaymentService;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/credit")
public class CreditApplicationController {
    private final CreditApplicationService creditApplicationService;
    private final EmployeeVerificationService employeeVerificationService;

    private final CreditPaymentService creditPaymentService;

    @PostMapping("/loan/create")
    public ResponseEntity<CreditApplicationResponseDTO> applyForCredit(
            @RequestBody CreditApplicationRequestDTO request,
            @RequestHeader("userId") String clientUuid) throws Exception {
        log.info("Запрос на создание кредита от пользователя {}", clientUuid);
        if (clientUuid == null) {
            throw new SecurityException("Invalid token");
        }
        boolean isVerified = employeeVerificationService.verifyEmployee(clientUuid);
        if (!isVerified) {
            throw new ForbiddenAccessException("Client is blocked");
        }

        CreditApplicationResponseDTO response = creditApplicationService.processApplication(request, clientUuid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/loan/list")
    public ResponseEntity<UserLoansResponseDTO> getUserLoans(
            @RequestHeader("userId") String clientUuid,
            @RequestParam int pageSize,
            @RequestParam int pageNumber) {

        UserLoansResponseDTO response = creditApplicationService.getUserLoans(clientUuid, pageSize, pageNumber);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/loan/{userId}/list")
    public ResponseEntity<UserLoansResponseDTO> getUserLoansFromEmployee(
            @RequestHeader("userId") String clientUuid,
            @PathVariable String userId,
            @RequestParam int pageSize,
            @RequestParam int pageNumber) throws Exception {

        if (clientUuid == null) {
            throw new SecurityException("Invalid token");
        }
        boolean isVerified = employeeVerificationService.verifyEmployee(clientUuid);
        if (!isVerified) {
            throw new ForbiddenAccessException("Client is blocked");
        }
        UserLoansResponseDTO response = creditApplicationService.getUserLoans(userId, pageSize, pageNumber);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/loan/{loanId}/pay")
    public ResponseEntity<String> payCredit(@RequestBody CreditPaymentRequestDTO request) throws Exception {
        boolean success = creditPaymentService.processPayment(request, PaymentStatus.MANUAL);
        return success ? ResponseEntity.ok("Платёж успешно проведён") :
                ResponseEntity.badRequest().body("Платёж не одобрен");
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<UserLoansResponseDTO.LoanDTO> getloanByNumber(@PathVariable String loanId) {
        UserLoansResponseDTO.LoanDTO credit = creditApplicationService.getCreditByNumber(loanId);
        return ResponseEntity.ok(credit);
    }
}
