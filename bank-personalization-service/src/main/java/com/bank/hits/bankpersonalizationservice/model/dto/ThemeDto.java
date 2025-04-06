package com.bank.hits.bankpersonalizationservice.model.dto;

import com.bank.hits.bankpersonalizationservice.common.enums.Theme;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThemeDto {

    private Theme theme;
}
