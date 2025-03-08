package com.bank.hits.bankuserservice.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class IncorrectActionException extends RuntimeException {
    public IncorrectActionException(String message) { super(message); }
}
