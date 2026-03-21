package com.hyuns.cafit.application.caffeine.dto;

import com.hyuns.cafit.application.beverage.dto.BeverageInfo;

public record DrinkCheckResponse(
        BeverageInfo beverage,
        CaffeineStatus before,
        CaffeineStatus after,
        UserCaffeineSettings settings,
        DrinkRecommendation recommendation,
        boolean isSafe
) {
}
