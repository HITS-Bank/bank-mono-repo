package com.bank.hits.bankuserservice.service;

import com.bank.hits.bankuserservice.model.entity.ClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakClientService {

    private final KeycloakAuthService keycloakAuthService;
    private final KeycloakUrlProvider keycloakUrlProvider;

    private final String configFilePath = "src/main/resources/client-config.json";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${keycloak.bank-client-id}")
    private String keycloakBankClientId;

    public String getClientUUID() throws IOException {
        ClientConfig config = loadClientConfig();
        if (config.getClientUUID() != null) {
            return config.getClientUUID();
        }

        String UUID = fetchClientUUIDFromKeycloak();
        saveClientConfig(UUID);
        return UUID;
    }

    private ClientConfig loadClientConfig() throws IOException {
        File file = new File(configFilePath);
        if (file.exists()) {
            return objectMapper.readValue(file, ClientConfig.class);
        }

        return new ClientConfig();
    }

    private void saveClientConfig(String UUID) throws IOException {
        ClientConfig config = new ClientConfig();
        config.setClientUUID(UUID);

        File file = new File(configFilePath);
        file.getParentFile().mkdirs();
        file.createNewFile();

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }

        objectMapper.writeValue(file, config);
    }

    private String fetchClientUUIDFromKeycloak() {
        String token = keycloakAuthService.getAdminToken();
        String url = keycloakUrlProvider.getClientsBaseUrl() + "?client_id=" + keycloakBankClientId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<ClientResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientResponse[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            ClientResponse[] clients = response.getBody();
            if (clients != null) {
                for (ClientResponse client : clients) {
                    if (keycloakBankClientId.equals(client.getClientId())) {
                        return client.getId();
                    }
                }
                throw new RuntimeException("Client with specified clientId not found");
            } else {
                throw new RuntimeException("No clients found");
            }
        } else {
            throw new RuntimeException("Failed to fetch clients");
        }
    }
    @Getter
    private static class ClientResponse {
        private String id;
        private String clientId;
    }
}
