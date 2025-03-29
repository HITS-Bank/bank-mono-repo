package com.bank.hits.bankuserservice.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KeycloakUserResponse {

    private String id;
    private String firstName;
    private String lastName;
    private Attributes attributes;

    @Data
    public static class Attributes {
        @JsonProperty("isBanned")
        private List<String> isBanned;
    }
}
