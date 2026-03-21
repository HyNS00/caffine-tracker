package com.hyuns.cafit.application.caffeine.dto;

public record CurrentCaffeineResponse(
        CaffeineStatus status,
        UserCaffeineSettings settings,
        DrinkRecommendation recommendation
) {}
