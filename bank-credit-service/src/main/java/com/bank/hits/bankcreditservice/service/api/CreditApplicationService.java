package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationRequestDTO;
import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationResponseDTO;
import com.bank.hits.bankcreditservice.model.DTO.UserLoansResponseDTO;

import java.util.UUID;

public interface CreditApplicationService {
    CreditApplicationResponseDTO processApplication(CreditApplicationRequestDTO request, String clientUuid) throws Exception;
    public UserLoansResponseDTO getUserLoans(String clientUuid, int pageSize, int pageNumber);

    public UserLoansResponseDTO.LoanDTO getCreditById(UUID number);
}
