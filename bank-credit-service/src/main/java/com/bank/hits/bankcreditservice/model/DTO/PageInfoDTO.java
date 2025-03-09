package com.bank.hits.bankcreditservice.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageInfoDTO {
    @JsonProperty("pageSize")
    private int pageSize;
    @JsonProperty("pageNumber")
    private int pageNumber;
}
