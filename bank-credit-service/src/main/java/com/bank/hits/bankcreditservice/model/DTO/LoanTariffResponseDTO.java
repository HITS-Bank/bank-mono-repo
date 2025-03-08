package com.bank.hits.bankcreditservice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoanTariffResponseDTO {
    private List<CreditTariffDTO> loanTariffs;
    private PageInfoDTO pageInfo;
}
