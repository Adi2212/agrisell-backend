package com.agridev.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

// Global exception handler to manage application level exceptions
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Handle UserNotFound custom exception
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<?> handleUserNotFound(UserNotFound ex) {

        log.error("UserNotFound Exception: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "User Not Found",
                ex.getMessage()
        );
    }

    // Handle validation errors (@Valid RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        log.warn("Validation failed: {}", ex.getMessage());

        Map<String, Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Validation Failed");

        Map<String, String> fieldErrors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(field ->
                fieldErrors.put(field.getField(), field.getDefaultMessage())
        );

        error.put("fields", fieldErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Handle validation errors (@PathVariable, @RequestParam)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {

        log.warn("Constraint violation: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Constraint Violation",
                ex.getMessage()
        );
    }

    // Handle IllegalArgumentException
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {

        log.warn("IllegalArgumentException: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                ex.getMessage()
        );
    }

    // Handle RuntimeException (Business Errors)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntime(RuntimeException ex) {

        log.error("Runtime Exception: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Runtime Error",
                ex.getMessage()
        );
    }

    // Handle Access Denied (Spring Security)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {

        log.error("Access Denied: {}", ex.getMessage());

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Access Denied",
                "You are not authorized to access this resource"
        );
    }

    // Handle all other generic exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {

        log.error("Internal Server Error: {}", ex.getMessage(), ex);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please try again later."
        );
    }

    // Reusable error response builder
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status,
            String errorType,
            String message
    ) {

        Map<String, Object> error = new HashMap<>();

        error.put("timestamp", LocalDateTime.now());
        error.put("status", status.value());
        error.put("error", errorType);
        error.put("message", message);

        return new ResponseEntity<>(error, status);
    }
}
