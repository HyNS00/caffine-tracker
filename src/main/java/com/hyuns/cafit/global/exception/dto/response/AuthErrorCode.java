package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum AuthErrorCode implements ErrorCode {

    UNAUTHORIZED("A00", "로그인이 필요합니다", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("A01", "이메일 또는 비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    AuthErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
