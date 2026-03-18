package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum IntakeErrorCode implements ErrorCode {

    INTAKE_NOT_FOUND("I00", "섭취 기록을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_INTAKE_ACCESS("I01", "해당 기록에 대한 권한이 없습니다", HttpStatus.FORBIDDEN);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    IntakeErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
