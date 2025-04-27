package com.bank.hits.bankcoreservice.core.utils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.bank.hits.bankcoreservice.core.entity.IdempotentResponse;
import com.bank.hits.bankcoreservice.core.repository.IdempotencyRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class IdempotencyUtils {

    private static final Map<String, Object> locks = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final IdempotencyRepository repository;

    public <T> ResponseEntity<T> handleIdempotency(final UUID key, final Supplier<ResponseEntity<T>> endpoint) {
        final Object lock = locks.computeIfAbsent(key.toString(), k -> new Object());
        synchronized (lock) {
            final ResponseEntity<T> cachedResponse = getResponse(key);
            if (cachedResponse != null) {
                return cachedResponse;
            }
            final ResponseEntity<T> response = endpoint.get();
            storeResponse(key, response);
            return response;
        }
    }

    public <T> void storeResponse(final UUID key, final T response) {
        try {
            final IdempotentResponse idempotentResponse = new IdempotentResponse();
            idempotentResponse.setId(key);
            if (response != null) {
                idempotentResponse.setResponseBody(objectMapper.writeValueAsString(response));
            }
            repository.save(idempotentResponse);
        } catch (final JsonProcessingException e) {
            log.error("Error saving idempotent response (kafka)", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T tryGetResponse(final UUID key, final T defaultValue) {
        final IdempotentResponse idempotentResponse = repository.findById(key).orElse(null);
        if (idempotentResponse == null) {
            return null;
        }

        try {
            if (idempotentResponse.getResponseBody() == null) {
                return defaultValue;
            }
            return (T) objectMapper.readValue(idempotentResponse.getResponseBody(), Object.class);
        } catch (JsonProcessingException e) {
            log.error("Error reading idempotent response (kafka)", e);
            throw new RuntimeException(e);
        }
    }

    private <T> void storeResponse(final UUID key, final ResponseEntity<T> response) {
        try {
            log.info("Saving idempotent response {}", key);
            final IdempotentResponse idempotentResponse = new IdempotentResponse();
            idempotentResponse.setId(key);
            if (response.hasBody()) {
                log.info("Response has body");
                idempotentResponse.setResponseBody(objectMapper.writeValueAsString(response.getBody()));
            }
            idempotentResponse.setResponseHeaders(objectMapper.writeValueAsString(response.getHeaders()));
            idempotentResponse.setResponseStatus(response.getStatusCode().value());
            log.info("Idempotent response {} body: {}", key, idempotentResponse.getResponseBody());
            repository.save(idempotentResponse);
        } catch (final JsonProcessingException e) {
            log.error("Error saving idempotent response", e);
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> getResponse(final UUID key) {
        final IdempotentResponse idempotentResponse = repository.findById(key).orElse(null);
        log.info("Getting idempotent response for key {}", key);
        if (idempotentResponse == null) {
            return null;
        }
        log.info("Response is not null");

        try {
            final ResponseEntity.BodyBuilder builder = ResponseEntity.status(idempotentResponse.getResponseStatus())
                    .headers(objectMapper.readValue(idempotentResponse.getResponseHeaders(), HttpHeaders.class));

            if (idempotentResponse.getResponseBody() != null) {
                log.info("Idempotent response body {} {}", key, idempotentResponse.getResponseBody());
                return builder.body((T) objectMapper.readValue(idempotentResponse.getResponseBody(), Object.class));
            }

            return builder.build();
        } catch (final JsonProcessingException e) {
            log.error("Error reading idempotent response", e);
            throw new RuntimeException(e);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void cleanupIdempotency() {
        repository.findAll().forEach((idempotentResponse) -> {
            if (idempotentResponse.getCreatedAt().isBefore(LocalDateTime.now().minusDays(1))) {
                repository.delete(idempotentResponse);
                locks.remove(idempotentResponse.getId().toString());
            }
        });
    }
}
