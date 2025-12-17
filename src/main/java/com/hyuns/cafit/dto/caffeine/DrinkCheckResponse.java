package com.hyuns.cafit.dto.caffeine;

import com.hyuns.cafit.dto.beverage.BeverageInfo;

public record DrinkCheckResponse(
        BeverageInfo beverage,
        CaffeineStatus before,
        CaffeineStatus after,
        UserCaffeineSettings settings,
        DrinkRecommendation recommendation,
        boolean isSafe
) {
}
