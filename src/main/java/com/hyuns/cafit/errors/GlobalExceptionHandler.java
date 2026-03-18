package com.hyuns.cafit.errors;

import com.hyuns.cafit.global.exception.dto.response.AuthErrorCode;
import com.hyuns.cafit.global.exception.dto.response.BeverageErrorCode;
import com.hyuns.cafit.global.exception.dto.response.DefaultErrorCode;
import com.hyuns.cafit.global.exception.dto.response.ErrorCode;
import com.hyuns.cafit.global.exception.dto.response.ExceptionResponse;
import com.hyuns.cafit.global.exception.dto.response.FavoriteErrorCode;
import com.hyuns.cafit.global.exception.dto.response.IntakeErrorCode;
import com.hyuns.cafit.global.exception.dto.response.UserErrorCode;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleException(Exception ex) {
        log.error("Exception : ", ex);
        return createResponseEntity(DefaultErrorCode.UNKNOWN_SERVER_EXCEPTION);
    }

    // Auth
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Object> handleUnauthorizedException(UnauthorizedException ex) {
        log.info("UnauthorizedException : {}", ex.getMessage());
        return createResponseEntity(AuthErrorCode.UNAUTHORIZED);
    }

    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<Object> handleLoginFailedException(LoginFailedException ex) {
        log.info("LoginFailedException : {}", ex.getMessage());
        return createResponseEntity(AuthErrorCode.LOGIN_FAILED);
    }

    // User
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex) {
        log.info("UserNotFoundException : {}", ex.getMessage());
        return createResponseEntity(UserErrorCode.USER_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Object> handleDuplicateEmailException(DuplicateEmailException ex) {
        log.info("DuplicateEmailException : {}", ex.getMessage());
        return createResponseEntity(UserErrorCode.DUPLICATE_EMAIL);
    }

    // Beverage
    @ExceptionHandler(BeverageNotFoundException.class)
    public ResponseEntity<Object> handleBeverageNotFoundException(BeverageNotFoundException ex) {
        log.info("BeverageNotFoundException : {}", ex.getMessage());
        return createResponseEntity(BeverageErrorCode.BEVERAGE_NOT_FOUND);
    }

    @ExceptionHandler(CustomBeverageNotFoundException.class)
    public ResponseEntity<Object> handleCustomBeverageNotFoundException(CustomBeverageNotFoundException ex) {
        log.info("CustomBeverageNotFoundException : {}", ex.getMessage());
        return createResponseEntity(BeverageErrorCode.CUSTOM_BEVERAGE_NOT_FOUND);
    }

    @ExceptionHandler(BeverageAccessDeniedException.class)
    public ResponseEntity<Object> handleBeverageAccessDeniedException(BeverageAccessDeniedException ex) {
        log.info("BeverageAccessDeniedException : {}", ex.getMessage());
        return createResponseEntity(BeverageErrorCode.UNAUTHORIZED_BEVERAGE_ACCESS);
    }

    // Intake
    @ExceptionHandler(IntakeNotFoundException.class)
    public ResponseEntity<Object> handleIntakeNotFoundException(IntakeNotFoundException ex) {
        log.info("IntakeNotFoundException : {}", ex.getMessage());
        return createResponseEntity(IntakeErrorCode.INTAKE_NOT_FOUND);
    }

    @ExceptionHandler(IntakeAccessDeniedException.class)
    public ResponseEntity<Object> handleIntakeAccessDeniedException(IntakeAccessDeniedException ex) {
        log.info("IntakeAccessDeniedException : {}", ex.getMessage());
        return createResponseEntity(IntakeErrorCode.UNAUTHORIZED_INTAKE_ACCESS);
    }

    // Favorite
    @ExceptionHandler(FavoriteNotFoundException.class)
    public ResponseEntity<Object> handleFavoriteNotFoundException(FavoriteNotFoundException ex) {
        log.info("FavoriteNotFoundException : {}", ex.getMessage());
        return createResponseEntity(FavoriteErrorCode.FAVORITE_NOT_FOUND);
    }

    @ExceptionHandler(DuplicateFavoriteException.class)
    public ResponseEntity<Object> handleDuplicateFavoriteException(DuplicateFavoriteException ex) {
        log.info("DuplicateFavoriteException : {}", ex.getMessage());
        return createResponseEntity(FavoriteErrorCode.DUPLICATE_FAVORITE);
    }

    @ExceptionHandler(FavoriteAccessDeniedException.class)
    public ResponseEntity<Object> handleFavoriteAccessDeniedException(FavoriteAccessDeniedException ex) {
        log.info("FavoriteAccessDeniedException : {}", ex.getMessage());
        return createResponseEntity(FavoriteErrorCode.UNAUTHORIZED_FAVORITE_ACCESS);
    }

    @ExceptionHandler(InvalidFavoriteListException.class)
    public ResponseEntity<Object> handleInvalidFavoriteListException(InvalidFavoriteListException ex) {
        log.info("InvalidFavoriteListException : {}", ex.getMessage());
        return createResponseEntity(FavoriteErrorCode.INVALID_FAVORITE_LIST);
    }

    // Validation
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        String errorMessage = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        DefaultErrorCode errorCode = DefaultErrorCode.API_ARGUMENTS_NOT_VALID;
        ExceptionResponse response = new ExceptionResponse(
                errorCode.getErrorCode(),
                errorMessage
        );

        return handleExceptionInternal(
                ex,
                response,
                headers,
                errorCode.getHttpStatus(),
                request
        );
    }

    // Static resource
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        log.debug("Static resource not found: {}", ex.getMessage());
        return createResponseEntity(DefaultErrorCode.UNKNOWN_SERVER_EXCEPTION);
    }

    private ResponseEntity<Object> createResponseEntity(ErrorCode errorCode) {
        ExceptionResponse response = ExceptionResponse.from(errorCode);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(response);
    }
}
