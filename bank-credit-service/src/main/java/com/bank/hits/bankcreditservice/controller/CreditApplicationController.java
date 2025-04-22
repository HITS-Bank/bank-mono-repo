package com.bank.hits.bankcreditservice.controller;

import com.bank.hits.bankcreditservice.config.JwtUtils;
import com.bank.hits.bankcreditservice.exception.ForbiddenAccessException;
import com.bank.hits.bankcreditservice.model.DTO.*;
import com.bank.hits.bankcreditservice.service.api.CreditApplicationService;
import com.bank.hits.bankcreditservice.service.api.CreditPaymentService;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import static com.bank.hits.bankcreditservice.exception.ExceptionUtils.throwExceptionRandomly;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/credit")
public class CreditApplicationController {
    private final CreditApplicationService creditApplicationService;
    private final EmployeeVerificationService employeeVerificationService;

    private final CreditPaymentService creditPaymentService;

    private final JwtUtils jwtUtils;

    @PostMapping("/loan/create")
    public ResponseEntity<CreditApplicationResponseDTO> applyForCredit(
            @RequestBody CreditApplicationRequestDTO request,
            HttpServletRequest httpServletRequest
    ) throws Exception {
        throwExceptionRandomly();

        String clientUuid = jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest));
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
    public ResponseEntity<List<UserLoansResponseDTO.LoanDTO>> getUserLoans(
            @RequestParam int pageSize,
            @RequestParam int pageNumber,
            HttpServletRequest httpServletRequest) {
        throwExceptionRandomly();

        String clientUuid = jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest));
        UserLoansResponseDTO response = creditApplicationService.getUserLoans(clientUuid, pageSize, pageNumber);
        return ResponseEntity.ok(response.getLoans());
    }

    @GetMapping("/employee/loan/{userId}/list")
    public ResponseEntity<List<UserLoansResponseDTO.LoanDTO>> getUserLoansFromEmployee(
            HttpServletRequest httpServletRequest,
            @PathVariable String userId,
            @RequestParam int pageSize,
            @RequestParam int pageNumber) throws Exception {
        throwExceptionRandomly();

        String clientUuid = jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest));
        if (clientUuid == null) {
            throw new SecurityException("Invalid token");
        }
        boolean isVerified = employeeVerificationService.verifyEmployee(clientUuid);
        if (!isVerified) {
            throw new ForbiddenAccessException("Client is blocked");
        }
        UserLoansResponseDTO response = creditApplicationService.getUserLoans(userId, pageSize, pageNumber);
        return ResponseEntity.ok(response.getLoans());
    }

    @PostMapping("/loan/{loanId}/pay")
    public ResponseEntity<String> payCredit(@PathVariable UUID loanId, @RequestBody CreditPaymentRequestDTO request) throws Exception {
        throwExceptionRandomly();

        boolean success = creditPaymentService.processPayment(loanId,request, PaymentStatus.MANUAL);
        return success ? ResponseEntity.ok("Платёж успешно проведён") :
                ResponseEntity.badRequest().body("Платёж не одобрен");
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<UserLoansResponseDTO.LoanDTO> getloanById(@PathVariable UUID loanId) {
        throwExceptionRandomly();

        UserLoansResponseDTO.LoanDTO credit = creditApplicationService.getCreditById(loanId);
        return ResponseEntity.ok(credit);
    }
}
