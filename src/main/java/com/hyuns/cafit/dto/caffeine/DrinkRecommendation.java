package com.hyuns.cafit.dto.caffeine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DrinkRecommendation {
    SAFE("안전 🙂", "일일 카페인 섭취량이 권장 범위 내입니다"),
    WARNING("주의 ⚠️", "일일 카페인 섭취량이 권장 한도에 근접했습니다"),
    DANGER("위험 🚫", "일일 카페인 섭취량이 권장 한도를 초과했습니다");

    private final String label;
    private final String description;
}
