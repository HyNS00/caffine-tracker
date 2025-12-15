package com.hyuns.cafit.domain.beverage;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BeverageCategory {
    // 커피류
    ESPRESSO("에스프레소", 250.0, 40),
    AMERICANO("아메리카노", 34.0, 355),
    LATTE("라떼", 38.0, 355),
    CAPPUCCINO("카푸치노", 40.0, 355),
    MOCHA("모카", 37.0, 355),
    COLD_BREW("콜드브루", 24.0, 355),

    // 스무디
    COFFEE_SMOOTHIE("커피 스무디", 21.5, 473),
    FRUIT_SMOOTHIE("과일 스무디", 0.0, 473),

    // 에너지/차류
    ENERGY_DRINK("에너지 음료", 29.0, 250),
    BLACK_TEA("홍차", 15.0, 200),
    GREEN_TEA("녹차", 13.0, 200),
    MILK_TEA("밀크티", 12.0, 355),

    // 기타
    ICED_TEA("아이스티", 4.0, 355);

    private final String displayName;
    private final double caffeineMgPer100ml;
    private final int defaultServingSizeMl;

    public double getDefaultTotalCaffeine() {
        return calculateTotalCaffeine(defaultServingSizeMl);
    }

    public double calculateCaffeineForVolume(int volumeMl) {
        return calculateTotalCaffeine(volumeMl);
    }

    private double calculateTotalCaffeine(int servingSizeMl) {
        if (servingSizeMl <= 0) {
            servingSizeMl = this.defaultServingSizeMl;
        }
        return (caffeineMgPer100ml * servingSizeMl) / 100.0;
    }

    public boolean hasCaffeine() {
        return caffeineMgPer100ml > 0;
    }
}
