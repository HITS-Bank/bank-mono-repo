package com.bank.hits.bankuserservice.user_service.service;

import com.bank.hits.bankuserservice.user_service.model.ClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakClientService {

    private final String configFilePath = "src/main/resources/client-config.json";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String getClientUUID() throws IOException {
        log.info("entered getClientUUID");
        ClientConfig config = loadClientConfig();
        if (config.getClientUUID() != null) {
            log.info("ОГО!!!!!! getClientUUID НЕ НУЛЛ!!");
            return config.getClientUUID();
        }

        String UUID = fetchClientUUIDFromKeycloak();
        saveClientConfig(UUID);
        return UUID;
    }

    private ClientConfig loadClientConfig() throws IOException {
        log.info("Зашли в loadClientConfig");
        File file = new File(configFilePath);
        if (file.exists()) {
            log.info("файл с конфигом уже есть");
            return objectMapper.readValue(file, ClientConfig.class);
        }
        return new ClientConfig();
    }

    private void saveClientConfig(String UUID) throws IOException {
        log.info("зашли в saveClientConfig с UUID " + UUID);
        ClientConfig config = new ClientConfig();
        config.setClientUUID(UUID);
        File file = new File(configFilePath);
        file.getParentFile().mkdirs();
        file.createNewFile();
        if (!file.exists()) {
            log.info("файла не существует лох");
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        objectMapper.writeValue(file, config);
    }

    private String fetchClientUUIDFromKeycloak() {
        // TODO тут и в других местах поменять на использование KeycloakAuthService::getAdminToken
        log.info("зашли в fetchClientUUIDFromKeycloak");
        String token = getAdminToken();
        String url = "http://keycloak:8080/admin/realms/bank/clients?client_id=bank-rest-api";
        log.info("получили токен админа " + token);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ClientResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientResponse[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            log.info("СТАТУС ОТВЕТА ОК!!! РЕАЛЬНО???");
            ClientResponse[] clients = response.getBody();
            if (clients != null) {
                for (ClientResponse client : clients) {
                    if ("bank-rest-api".equals(client.getClientId())) {
                        log.info("Нашли client-id для bank-rest-api " + client.getId());
                        return client.getId();
                    }
                }
                throw new RuntimeException("Client with clientId 'bank-rest-api' not found");
            } else {
                throw new RuntimeException("No clients found");
            }
        } else {
            throw new RuntimeException("Failed to fetch clients");
        }
    }

    private String getAdminToken() {
        log.info("зашли в getAdminToken");
        String url = "http://keycloak:8080/realms/master/protocol/openid-connect/token";
        String body = "grant_type=password&username=admin&password=admin&client_id=admin-cli";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        ResponseEntity<TokenResponse> response = restTemplate.postForEntity(url, request, TokenResponse.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody().getAccess_token();
        } else {
            throw new RuntimeException("Failed to obtain admin token");
        }
    }

    @Getter
    private static class ClientResponse {
        private String id;
        private String clientId;
    }

    @Getter
    private static class TokenResponse {
        private String access_token;
    }
}
