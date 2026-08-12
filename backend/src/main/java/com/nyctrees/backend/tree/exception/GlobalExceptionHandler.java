package com.nyctrees.backend.tree.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates known service-layer exceptions into appropriate HTTP responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Reports interaction capacity exhaustion as 503 so clients can distinguish
     * it from a generic server error and show a dedicated message.
     *
     * @param ex the thrown capacity exception
     * @return 503 response with the exception's message as plain text body
     */
    @ExceptionHandler(InteractionCapacityExceededException.class)
    public ResponseEntity<String> handleCapacityExceeded(InteractionCapacityExceededException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
    }
}
