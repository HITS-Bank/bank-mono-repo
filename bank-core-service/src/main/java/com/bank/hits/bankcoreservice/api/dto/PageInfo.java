package com.bank.hits.bankcoreservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageInfo {
    int pageSize;
    int pageNumber;
}
