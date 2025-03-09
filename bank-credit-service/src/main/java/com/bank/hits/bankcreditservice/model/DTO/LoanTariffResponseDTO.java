package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTariffResponseDTO {
    @JsonProperty("loanTariffs")
    private List<CreditTariffDTO> loanTariffs;
    @JsonProperty("pageInfo")
    private PageInfoDTO pageInfo;
}
