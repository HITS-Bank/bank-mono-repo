package com.bank.hits.bankuserservice.auth.dto;

public record LoginRequest(
        String email,
        String password
) {
}
