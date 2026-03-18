package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND("U00", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("U01", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    UserErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
