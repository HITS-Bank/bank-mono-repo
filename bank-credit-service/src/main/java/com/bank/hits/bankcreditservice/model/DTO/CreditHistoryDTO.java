package com.bank.hits.bankcreditservice.model.DTO;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreditHistoryDTO {
    BigDecimal totalCreditAmount;
    List<CreditRecordDTO> creditRecords;

}
