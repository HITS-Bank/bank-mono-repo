package com.bank.hits.bankcoreservice.core.service;

import com.bank.hits.bankcoreservice.api.dto.CurrencyCode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class CurrencyConversionService {
    private final RestTemplate restTemplate;

    @Autowired
    public CurrencyConversionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    private static final String EXCHANGE_API =
            "https://api.apilayer.com/exchangerates_data/convert?from={from}&to={to}&amount={amount}";

    public BigDecimal convert(CurrencyCode from, CurrencyCode to, BigDecimal amount) {
        if (from == to) return amount;

        Map<String, String> params = Map.of(
                "from", from.name(),
                "to", to.name(),
                "amount", amount.toPlainString()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("apikey", "dqz0Papq7Qsm9QkwwvDOoLNJIIQPXZAR");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        log.info("до response");
        ResponseEntity<JsonNode> response = restTemplate.exchange(
                EXCHANGE_API,
                HttpMethod.GET,
                entity,
                JsonNode.class,
                params
        );
        log.info("после response, body: " + response.getBody());

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            BigDecimal result = new BigDecimal(response.getBody().get("result").asText());
            return result;
        }

        throw new IllegalStateException("Currency conversion failed");
    }
}
