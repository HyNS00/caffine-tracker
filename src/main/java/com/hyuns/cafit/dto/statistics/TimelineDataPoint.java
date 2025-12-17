package com.hyuns.cafit.dto.statistics;

import java.time.LocalDateTime;

public record TimelineDataPoint(
        LocalDateTime time,
        double caffeineMg
) {
}
