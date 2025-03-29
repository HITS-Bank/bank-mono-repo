package com.bank.hits.bankuserservice.user_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakAuthService {

    private final RestTemplate restTemplate;

    private final KeycloakUrlProvider keycloakUrlProvider;

    @Value("${keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${keycloak.admin-client-secret}")
    private String adminClientSecret;

    private String accessToken;
    private Instant tokenExpiryTime;

    public synchronized String getAdminToken() {
        if (accessToken != null && tokenExpiryTime != null && Instant.now().isBefore(tokenExpiryTime)) {
            return accessToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=client_credentials" +
                "&client_id=" + adminClientId +
                "&client_secret=" + adminClientSecret;

        log.info("tokenUrl = " + keycloakUrlProvider.getTokenUrl());

        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                keycloakUrlProvider.getTokenUrl(),
                HttpMethod.POST,
                request,
                Map.class
        );

        if (response.getBody() != null) {
            accessToken = (String) response.getBody().get("access_token");
            int expiresIn = (Integer) response.getBody().get("expires_in");
            tokenExpiryTime = Instant.now().plusSeconds(expiresIn - 30);
        } else {
            throw new RuntimeException("Не удалось получить access_token для админа Keycloak");
        }

        return accessToken;
    }
}
