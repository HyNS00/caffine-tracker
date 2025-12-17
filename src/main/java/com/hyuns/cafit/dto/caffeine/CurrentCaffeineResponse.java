package com.hyuns.cafit.dto.caffeine;

public record CurrentCaffeineResponse(
        CaffeineStatus status,
        UserCaffeineSettings settings,
        DrinkRecommendation recommendation
) {}
