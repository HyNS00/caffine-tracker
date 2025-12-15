package com.hyuns.cafit.errors;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorMessage {
    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다"),
    LOGIN_FAILED("이메일 또는 비밀번호가 일치하지 않습니다"),

    // Beverage
    BEVERAGE_NOT_FOUND("음료를 찾을 수 없습니다"),
    INVALID_CATEGORY("유효하지 않은 카테고리입니다"),

    // CustomBeverage
    CUSTOM_BEVERAGE_NOT_FOUND("커스텀 음료를 찾을 수 없습니다"),
    UNAUTHORIZED_BEVERAGE_ACCESS("해당 음료에 대한 권한이 없습니다"),

    // Intake
    INTAKE_NOT_FOUND("섭취 기록을 찾을 수 없습니다"),
    UNAUTHORIZED_INTAKE_ACCESS("해당 기록에 대한 권한이 없습니다"),
    // Favorite
    INVALID_FAVORITE_BEVERAGE_TYPE("즐겨찾기 음료는 프리셋 또는 커스텀 중 하나만 설정되어야 합니다");

    private final String message;
}
