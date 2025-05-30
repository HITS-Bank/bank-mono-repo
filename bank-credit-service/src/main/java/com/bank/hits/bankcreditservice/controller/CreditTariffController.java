package com.bank.hits.bankcreditservice.controller;

import com.bank.hits.bankcreditservice.config.IdempotencyUtils;
import com.bank.hits.bankcreditservice.config.JwtUtils;
import com.bank.hits.bankcreditservice.exception.ForbiddenAccessException;
import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.CreditTariffDTO;
import com.bank.hits.bankcreditservice.model.DTO.LoanTariffResponseDTO;
import com.bank.hits.bankcreditservice.service.api.CreditTariffService;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.bank.hits.bankcreditservice.exception.ExceptionUtils.throwExceptionRandomly;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/credit")
public class CreditTariffController {
    private final CreditTariffService creditTariffService;
    private final EmployeeVerificationService employeeVerificationService;
    private final IdempotencyUtils idempotency;

    private final JwtUtils jwtUtils;

    @PostMapping("/employee/loan/tariffs/create")
    public ResponseEntity<?> createTariff(@RequestBody CreditTariff tariff,
                                          @RequestParam UUID requestId,
                                          HttpServletRequest httpServletRequest) {
        return idempotency.handleIdempotency(requestId, () -> {
            throwExceptionRandomly();

            String employeeUuid = jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest));
            log.info("Запрос на создание тарифа");
            if (employeeUuid == null) {
                throw new SecurityException("Invalid token");
            }
            log.info("Начинаем верификацию");
            boolean isVerified = false;
            try {
                isVerified = employeeVerificationService.verifyEmployee(employeeUuid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!isVerified) {
                throw new ForbiddenAccessException("Employee is blocked");
            }
            log.info("Верификация успешна");
            CreditTariff savedTariff = creditTariffService.saveTariff(tariff);
            CreditTariffDTO dto = creditTariffService.convertToDTO(savedTariff);
            return ResponseEntity.ok(dto);
        });
    }

    @DeleteMapping("/employee/loan/tariffs/{tariffId}/delete")
    public ResponseEntity<?> DeleteTariff(@PathVariable String tariffId,
                                          @RequestParam UUID requestId,
                                          HttpServletRequest httpServletRequest) throws Exception {
        return idempotency.handleIdempotency(requestId, () -> {
            throwExceptionRandomly();

            String employeeUuid = jwtUtils.getUserId(jwtUtils.extractAccessToken(httpServletRequest));
            log.info("Запрос на удаление тарифа от пользователя {}", employeeUuid);
            if (employeeUuid == null) {
                throw new SecurityException("Invalid token");
            }

            boolean isVerified = false;
            try {
                isVerified = employeeVerificationService.verifyEmployee(employeeUuid);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (!isVerified) {
                throw new ForbiddenAccessException("Employee is blocked");
            }

            boolean updated = creditTariffService.markTariffAsInactive(UUID.fromString(tariffId));
            if (!updated) {
                throw new NoSuchElementException("Тариф не найден");
            }

            return ResponseEntity.ok("Тариф успешно деактивирован");
        });
    }

    @GetMapping("/loan/tariffs")
    public ResponseEntity<List<CreditTariffDTO>> getActiveTariffs(
            @RequestParam(required = false, defaultValue = "") String nameQuery,
            @RequestParam String sortingProperty,
            @RequestParam String sortingOrder,
            @RequestParam int pageSize,
            @RequestParam int pageNumber) {
        throwExceptionRandomly();

        LoanTariffResponseDTO response = creditTariffService.getActiveTariffs(nameQuery, sortingProperty, sortingOrder, pageSize, pageNumber-1);
        return ResponseEntity.ok(response.getLoanTariffs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditTariff> getTariffById(@PathVariable UUID id) {
        throwExceptionRandomly();

        Optional<CreditTariff> tariff = creditTariffService.getTariffById(id);
        return tariff.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }



    /*
    private String extractUserIdFromJwt(String authHeader) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthToken) {
            return jwtAuthToken.getToken().getClaimAsString("userId");
        }
        return null;
    }

     */
}
