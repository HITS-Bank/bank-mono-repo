package com.bank.hits.bankuserservice.common.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.bank.hits.bankuserservice.common.exception.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            ForbiddenActionException.class,
    })
    public ResponseEntity<Map<String, Object>> handleCustomExceptions(RuntimeException ex) {
        HttpStatus status = determineStatus(ex);
        return new ResponseEntity<>(createErrorResponse(ex, status), status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(RuntimeException ex) {
        return new ResponseEntity<>(createErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private HttpStatus determineStatus(RuntimeException ex) {
        if (ex instanceof ForbiddenActionException) return HttpStatus.FORBIDDEN;
        return HttpStatus.BAD_REQUEST;
    }

    private Map<String, Object> createErrorResponse(Exception ex, HttpStatus status) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", ex.getMessage()
        );
    }
}
