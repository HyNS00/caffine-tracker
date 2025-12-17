package com.hyuns.cafit.dto.caffeine;

public record CaffeineStatus(
        double currentMg,
        double predictedAtBedtimeMg,
        double todayTotalMg,
        double hoursUntilBedtime
) {}
