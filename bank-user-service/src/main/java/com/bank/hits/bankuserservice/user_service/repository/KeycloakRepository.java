package com.bank.hits.bankuserservice.user_service.repository;

import com.bank.hits.bankuserservice.auth.dto.RegisterRequest;
import com.bank.hits.bankuserservice.common.model.KeycloakRoleResponse;
import com.bank.hits.bankuserservice.common.model.KeycloakUserResponse;
import com.bank.hits.bankuserservice.user_service.service.KeycloakAuthService;
import com.bank.hits.bankuserservice.user_service.service.KeycloakClientService;
import com.bank.hits.bankuserservice.user_service.service.KeycloakUrlProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class KeycloakRepository {

    private final RestTemplate restTemplate;

    private final KeycloakAuthService keycloakAuthService;
    private final KeycloakClientService keycloakClientService;
    private final KeycloakUrlProvider keycloakUrlProvider;

    public KeycloakUserResponse getUser(String userId) {
        String url = keycloakUrlProvider.getUsersBaseUrl() + "/" + userId;
        String adminToken = keycloakAuthService.getAdminToken();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(adminToken),
                new ParameterizedTypeReference<KeycloakUserResponse>() {}
        ).getBody();
    }

    public List<KeycloakRoleResponse> getUserRoles(String userId) {
        String clientUUID = getClientUUID();
        String url = keycloakUrlProvider.getUsersBaseUrl() + "/" + userId + "/role-mappings/clients/" + clientUUID;
        String adminToken = keycloakAuthService.getAdminToken();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(adminToken),
                new ParameterizedTypeReference<List<KeycloakRoleResponse>>() {}
        ).getBody();
    }

    public void updateUserAttributes(String userId, Map<String, Object> newAttributes) {
        String url = keycloakUrlProvider.getUsersBaseUrl() + "/" + userId;
        String adminToken = keycloakAuthService.getAdminToken();

        KeycloakUserResponse existingUser = getUser(userId);

        Map<String, Object> updatedUser = new HashMap<>();
        updatedUser.put("id", existingUser.getId());
        updatedUser.put("firstName", existingUser.getFirstName());
        updatedUser.put("lastName", existingUser.getLastName());
        updatedUser.put("enabled", true);
        updatedUser.put("attributes", newAttributes);

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                createHttpEntityWithBody(adminToken, updatedUser),
                Void.class
        );
    }

    public ResponseEntity<Void> createUser(RegisterRequest user) {
        String adminToken = keycloakAuthService.getAdminToken();

        Map<String, Object> userFields = new HashMap<>();
        userFields.putAll(
                Map.of(
                        "username", String.format("%s_%s", user.getFirstName(), user.getLastName()),
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "enabled", true
                )
        );

        return restTemplate.exchange(
                keycloakUrlProvider.getUsersBaseUrl(),
                HttpMethod.POST,
                createHttpEntityWithBody(adminToken, userFields),
                Void.class
        );
    }

    public void setPasswordForUser(String userId, String password) {
        String url = keycloakUrlProvider.getUsersBaseUrl() + "/" + userId + "/reset-password";
        String adminToken = keycloakAuthService.getAdminToken();

        Map<String, Object> changePasswordFields = new HashMap<>();
        changePasswordFields.putAll(
                Map.of(
                        "type", "password",
                        "value", password,
                        "temporary", false
                )
        );

        restTemplate.exchange(
                url,
                HttpMethod.PUT,
                createHttpEntityWithBody(adminToken, changePasswordFields),
                Void.class
        );
    }

    public List<KeycloakRoleResponse> getKeycloakClientRoles() {
        String clientUUID = getClientUUID();
        String url = keycloakUrlProvider.getClientsBaseUrl() + "/" + clientUUID + "/roles";
        String adminToken = keycloakAuthService.getAdminToken();

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                createHttpEntity(adminToken),
                new ParameterizedTypeReference<List<KeycloakRoleResponse>>() {}
        ).getBody();
    }

    public void assignRolesForUser(String userId, List<Map<String, String>> roles) {
        String clientUUID = getClientUUID();
        String url = keycloakUrlProvider.getUsersBaseUrl() + "/" + userId + "/role-mappings/clients/" + clientUUID;
        String adminToken = keycloakAuthService.getAdminToken();

        ResponseEntity<Void> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                createHttpEntityWithBody(adminToken, roles),
                Void.class
        );

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Ошибка назначения ролей пользователю " + userId);
        }
    }

    public List<KeycloakUserResponse> getUsers(
            int pageNumber,
            int pageSize,
            String role
    ) {
        String clientUUID = getClientUUID();
        String url = keycloakUrlProvider.getClientsBaseUrl() + "/" + clientUUID + "/roles/" + role + "/users";
        String adminToken = keycloakAuthService.getAdminToken();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("first", (pageNumber - 1) * pageSize)
                .queryParam("max", pageSize);

        return restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                createHttpEntity(adminToken),
                new ParameterizedTypeReference<List<KeycloakUserResponse>>() {}
        ).getBody();
    }

    private String getClientUUID() {
        try {
            return keycloakClientService.getClientUUID();
        } catch (IOException e) {
            throw new RuntimeException("Could not retrieve client UUID for Keycloak", e);
        }
    }

    private HttpEntity<Void> createHttpEntity(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Map<String, Object>> createHttpEntityWithBody(String token, Map<String, Object> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }

    private HttpEntity<List<Map<String, String>>> createHttpEntityWithBody(String token, List<Map<String, String>> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
    }
}
