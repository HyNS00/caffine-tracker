package com.hyuns.cafit.errors;

import com.hyuns.cafit.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(CafitException.class)
    public ResponseEntity<ErrorResponse> handleCafitException(CafitException e) {
        log.warn("CafitException occurred: {}", e.getMessage());

        ErrorMessage errorMessage = e.getErrorMessage();
        ErrorResponse response = ErrorResponse.of(
                errorMessage.getCode(),
                errorMessage.getMessage()
        );

        return ResponseEntity.status(errorMessage.getHttpStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation failed: {}", e.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.of(
                ErrorMessage.VALIDATION_FAILED.getCode(),
                ErrorMessage.VALIDATION_FAILED.getMessage(),
                fieldErrors
        );

        return ResponseEntity.status(ErrorMessage.VALIDATION_FAILED.getHttpStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected error occurred", e);

        ErrorMessage errorMessage = ErrorMessage.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(
                errorMessage.getCode(),
                errorMessage.getMessage()
        );

        return ResponseEntity.status(errorMessage.getHttpStatus()).body(response);
    }
}
