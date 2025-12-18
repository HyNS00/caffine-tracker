package com.hyuns.cafit.errors;

import com.hyuns.cafit.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

    // 추가
    /**
     * 정적 리소스를 찾을 수 없는 경우 (Chrome DevTools 등)
     * ERROR 대신 DEBUG 레벨로 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(NoResourceFoundException e) {
        log.debug("Static resource not found: {}", e.getMessage());

        ErrorResponse response = ErrorResponse.of(
                "NOT_FOUND",
                "요청한 리소스를 찾을 수 없습니다"
        );

        return ResponseEntity.status(404).body(response);
    }
}
