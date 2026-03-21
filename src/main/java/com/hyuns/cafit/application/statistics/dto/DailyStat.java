package com.hyuns.cafit.application.statistics.dto;

import java.time.LocalDate;

public record DailyStat(
        LocalDate date,
        double totalCaffeineMg,
        int intakeCount
) {
}
