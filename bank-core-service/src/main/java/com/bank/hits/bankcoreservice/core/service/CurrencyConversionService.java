package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.dto.CurrencyCode;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class CurrencyConversionService {
    private final RestTemplate restTemplate;

    @Autowired
    public CurrencyConversionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    private static final String EXCHANGE_API = "https://api.exchangerate.host/convert?from={from}&to={to}&amount={amount}";

    public BigDecimal convert(CurrencyCode from, CurrencyCode to, BigDecimal amount) {
        if (from == to) return amount;

        Map<String, String> params = Map.of(
                "from", from.name(),
                "to", to.name(),
                "amount", amount.toPlainString()
        );

        ResponseEntity<JsonNode> response = restTemplate.getForEntity(EXCHANGE_API, JsonNode.class, params);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BigDecimal result = new BigDecimal(response.getBody().get("result").asText());
            return result;
        }

        throw new IllegalStateException("Currency conversion failed");
    }
}
