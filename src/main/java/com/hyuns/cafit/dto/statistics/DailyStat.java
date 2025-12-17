package com.hyuns.cafit.dto.statistics;

import java.time.LocalDate;

public record DailyStat(
        LocalDate date,
        double totalCaffeineMg,
        int intakeCount
) {
}
