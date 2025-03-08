package com.bank.hits.bankcreditservice.service.api;

import com.bank.hits.bankcreditservice.model.CreditTariff;
import com.bank.hits.bankcreditservice.model.DTO.CreditTariffDTO;
import com.bank.hits.bankcreditservice.model.DTO.LoanTariffResponseDTO;

import java.util.Optional;
import java.util.UUID;

public interface CreditTariffService {
    public CreditTariff saveTariff(CreditTariff tariff);
    public Optional<CreditTariff> getTariffById(UUID id);

    public CreditTariffDTO convertToDTO(CreditTariff tariff);

    public boolean markTariffAsInactive(UUID id);

    public LoanTariffResponseDTO getActiveTariffs(String nameQuery, String sortingProperty, String sortingOrder, int pageSize, int pageNumber);
}
