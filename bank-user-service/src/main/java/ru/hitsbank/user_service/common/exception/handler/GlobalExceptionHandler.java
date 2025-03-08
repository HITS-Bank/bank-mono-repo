package ru.hitsbank.user_service.common.exception.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.hitsbank.user_service.common.exception.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            InvalidCredentialsException.class,
            UnauthorizedException.class,
            InitiatorUserNotFoundException.class,
            UserAlreadyExistsException.class,
            ForbiddenActionException.class,
            IncorrectActionException.class,
            UserNotFoundException.class,
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
        if (ex instanceof InvalidCredentialsException) return HttpStatus.UNAUTHORIZED;
        if (ex instanceof UnauthorizedException) return HttpStatus.UNAUTHORIZED;
        if (ex instanceof InitiatorUserNotFoundException) return HttpStatus.UNAUTHORIZED;
        if (ex instanceof UserAlreadyExistsException) return HttpStatus.CONFLICT;
        if (ex instanceof ForbiddenActionException) return HttpStatus.FORBIDDEN;
        if (ex instanceof IncorrectActionException) return HttpStatus.BAD_REQUEST;
        if (ex instanceof UserNotFoundException) return HttpStatus.NOT_FOUND;
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
