package com.hyuns.cafit.global.exception.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum FavoriteErrorCode implements ErrorCode {

    FAVORITE_NOT_FOUND("F00", "즐겨찾기를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_FAVORITE("F01", "이미 즐겨찾기에 추가된 음료입니다", HttpStatus.CONFLICT),
    UNAUTHORIZED_FAVORITE_ACCESS("F02", "해당 즐겨찾기에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_FAVORITE_BEVERAGE_TYPE("F03", "즐겨찾기 음료는 프리셋 또는 커스텀 중 하나만 설정되어야 합니다", HttpStatus.BAD_REQUEST),
    INVALID_FAVORITE_LIST("F04", "즐겨찾기 목록이 올바르지 않습니다", HttpStatus.BAD_REQUEST);

    private final String errorCode;
    private final String message;
    private final HttpStatus httpStatus;

    FavoriteErrorCode(String errorCode, String message, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
