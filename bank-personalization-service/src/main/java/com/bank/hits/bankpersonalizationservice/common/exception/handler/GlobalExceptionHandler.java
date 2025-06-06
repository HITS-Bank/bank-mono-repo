package com.bank.hits.bankpersonalizationservice.common.exception.handler;

import com.bank.hits.bankpersonalizationservice.common.exception.ForbiddenActionException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
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
