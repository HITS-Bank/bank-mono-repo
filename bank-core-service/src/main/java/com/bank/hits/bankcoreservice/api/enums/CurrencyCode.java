package com.bank.hits.bankcoreservice.api.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum CurrencyCode {
    RUB("RUB"),
    KZT("KZT"),
    CNY("CNY"),
    ;

    private final String value;

    CurrencyCode(final String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
