package com.hyuns.cafit.application.caffeine.dto;

public record CaffeineStatus(
        double currentMg,
        double predictedAtBedtimeMg,
        double todayTotalMg,
        double hoursUntilBedtime
) {}
