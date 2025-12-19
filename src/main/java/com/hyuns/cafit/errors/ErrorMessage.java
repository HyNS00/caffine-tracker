package com.hyuns.cafit.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    // Auth
    UNAUTHORIZED("UNAUTHORIZED", "로그인이 필요합니다", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("LOGIN_FAILED", "이메일 또는 비밀번호가 일치하지 않습니다", HttpStatus.UNAUTHORIZED),

    // User
    USER_NOT_FOUND("USER_NOT_FOUND", "사용자를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_EMAIL("DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다", HttpStatus.CONFLICT),

    // Beverage
    BEVERAGE_NOT_FOUND("BEVERAGE_NOT_FOUND", "음료를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    INVALID_CATEGORY("INVALID_CATEGORY", "유효하지 않은 카테고리입니다", HttpStatus.BAD_REQUEST),

    // CustomBeverage
    CUSTOM_BEVERAGE_NOT_FOUND("CUSTOM_BEVERAGE_NOT_FOUND", "커스텀 음료를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_BEVERAGE_ACCESS("UNAUTHORIZED_BEVERAGE_ACCESS", "해당 음료에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // Intake
    INTAKE_NOT_FOUND("INTAKE_NOT_FOUND", "섭취 기록을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_INTAKE_ACCESS("UNAUTHORIZED_INTAKE_ACCESS", "해당 기록에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),

    // Favorite
    INVALID_FAVORITE_BEVERAGE_TYPE("INVALID_FAVORITE_BEVERAGE_TYPE",
            "즐겨찾기 음료는 프리셋 또는 커스텀 중 하나만 설정되어야 합니다", HttpStatus.BAD_REQUEST),

    // Validation
    VALIDATION_FAILED("VALIDATION_FAILED", "입력값 검증 실패", HttpStatus.BAD_REQUEST),

    // System Error
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),

    // Favorite Beverage
    FAVORITE_NOT_FOUND("FAVORITE_NOT_FOUND", "즐겨찾기를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    DUPLICATE_FAVORITE("DUPLICATE_FAVORITE", "이미 즐겨찾기에 추가된 음료입니다", HttpStatus.CONFLICT),
    UNAUTHORIZED_FAVORITE_ACCESS("UNAUTHORIZED_FAVORITE_ACCESS", "해당 즐겨찾기에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),
    INVALID_FAVORITE_LIST("INVALID_FAVORITE_LIST", "즐겨찾기 목록이 올바르지 않습니다", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
