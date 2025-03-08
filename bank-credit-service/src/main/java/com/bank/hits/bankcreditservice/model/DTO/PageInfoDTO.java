package com.bank.hits.bankcreditservice.model.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfoDTO {
    private int pageSize;
    private int pageNumber;
}
