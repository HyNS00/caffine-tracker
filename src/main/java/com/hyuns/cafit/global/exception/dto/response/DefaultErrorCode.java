package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum DefaultErrorCode implements ErrorCode {

    UNKNOWN_SERVER_EXCEPTION("D00", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_INPUT("D01", "유효하지 않은 입력", HttpStatus.BAD_REQUEST),
    API_ARGUMENTS_NOT_VALID("D02", "잘못된 API 입력", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    DefaultErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
