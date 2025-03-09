package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationRequestDTO;
import com.bank.hits.bankcreditservice.model.DTO.CreditApplicationResponseDTO;
import com.bank.hits.bankcreditservice.model.DTO.UserLoansResponseDTO;

public interface CreditApplicationService {
    CreditApplicationResponseDTO processApplication(CreditApplicationRequestDTO request, String clientUuid) throws Exception;
    public UserLoansResponseDTO getUserLoans(String clientUuid, int pageSize, int pageNumber);

    public UserLoansResponseDTO.LoanDTO getCreditByNumber(String number);
}
