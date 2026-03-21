package com.hyuns.cafit.application.statistics.dto;

import java.time.LocalDateTime;

public record TimelineDataPoint(
        LocalDateTime time,
        double caffeineMg
) {
}
