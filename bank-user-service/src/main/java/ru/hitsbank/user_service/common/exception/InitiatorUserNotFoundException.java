package ru.hitsbank.user_service.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InitiatorUserNotFoundException extends RuntimeException {
    public InitiatorUserNotFoundException(String message) { super(message); }
}
