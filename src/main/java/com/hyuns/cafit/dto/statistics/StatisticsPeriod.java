package com.hyuns.cafit.dto.statistics;

import java.time.LocalDate;

public record StatisticsPeriod(
        LocalDate start,
        LocalDate end
) {}
