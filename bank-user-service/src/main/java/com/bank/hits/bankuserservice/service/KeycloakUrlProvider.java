package com.bank.hits.bankuserservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakUrlProvider {

    @Value("${keycloak.host-uri}")
    private String keycloakHostUri;

    @Value("${keycloak.realm-name}")
    private String keycloakBankRealmName;

    public String getClientsBaseUrl() {
        return keycloakHostUri + "/admin/realms/" + keycloakBankRealmName + "/clients";
    }

    public String getUsersBaseUrl() {
        return keycloakHostUri + "/admin/realms/" + keycloakBankRealmName + "/users";
    }

    public String getTokenUrl() {
        return keycloakHostUri + "/realms/" + keycloakBankRealmName + "/protocol/openid-connect/token";
    }
}
