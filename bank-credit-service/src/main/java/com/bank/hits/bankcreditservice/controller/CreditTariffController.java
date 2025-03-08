package com.bank.hits.bankcreditservice.controller;

import com.bank.hits.bankcreditservice.exception.ForbiddenAccessException;
import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.CreditTariffDTO;
import com.bank.hits.bankcreditservice.model.DTO.DeleteTariffDTO;
import com.bank.hits.bankcreditservice.model.DTO.LoanTariffResponseDTO;
import com.bank.hits.bankcreditservice.service.api.CreditTariffService;
import com.bank.hits.bankcreditservice.service.api.EmployeeVerificationService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/credit")
public class CreditTariffController {
    private final CreditTariffService creditTariffService;
    private final EmployeeVerificationService employeeVerificationService;

    @PostMapping("/employee/loan/tariffs/create")
    public ResponseEntity<?> createTariff(@RequestBody CreditTariff tariff,
                                          @RequestHeader("userId") String employeeUuid) throws Exception {
        if (employeeUuid == null) {
            throw new SecurityException("Invalid token");
        }

        boolean isVerified = employeeVerificationService.verifyEmployee(employeeUuid);
        if (!isVerified) {
            throw new ForbiddenAccessException("Employee is blocked");
        }

        CreditTariff savedTariff = creditTariffService.saveTariff(tariff);
        CreditTariffDTO dto = creditTariffService.convertToDTO(savedTariff);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/employee/loan/tariffs/delete")
    public ResponseEntity<?> DeleteTariff(@RequestBody DeleteTariffDTO dto,
                                          @RequestHeader("userId") String employeeUuid) throws Exception {
        log.info("Запрос на удаление тарифа от пользователя {}", employeeUuid);
        if (employeeUuid == null) {
            throw new SecurityException("Invalid token");
        }

        boolean isVerified = employeeVerificationService.verifyEmployee(employeeUuid);
        if (!isVerified) {
            throw new ForbiddenAccessException("Employee is blocked");
        }

        boolean updated = creditTariffService.markTariffAsInactive(dto.getTariffId());
        if (!updated) {
            throw new NoSuchElementException("Тариф не найден");
        }

        return ResponseEntity.ok("Тариф успешно деактивирован");
    }

    @GetMapping("/loan/tariffs")
    public ResponseEntity<LoanTariffResponseDTO> getActiveTariffs(
            @RequestParam(required = false, defaultValue = "") String nameQuery,
            @RequestParam String sortingProperty,
            @RequestParam String sortingOrder,
            @RequestParam int pageSize,
            @RequestParam int pageNumber) {

        LoanTariffResponseDTO response = creditTariffService.getActiveTariffs(nameQuery, sortingProperty, sortingOrder, pageSize, pageNumber-1);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditTariff> getTariffById(@PathVariable UUID id) {
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
