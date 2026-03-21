package com.hyuns.cafit.application.statistics.dto;

import java.time.LocalDate;

public record StatisticsPeriod(
        LocalDate start,
        LocalDate end
) {}
