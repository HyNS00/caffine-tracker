package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum BeverageErrorCode implements ErrorCode {

    BEVERAGE_NOT_FOUND("B00", "음료를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_CATEGORY("B01", "유효하지 않은 카테고리입니다", HttpStatus.BAD_REQUEST),
    CUSTOM_BEVERAGE_NOT_FOUND("B02", "커스텀 음료를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BEVERAGE_ACCESS("B03", "해당 음료에 대한 권한이 없습니다", HttpStatus.FORBIDDEN);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    BeverageErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
