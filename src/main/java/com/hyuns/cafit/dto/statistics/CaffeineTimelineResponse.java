package com.hyuns.cafit.dto.statistics;

import java.time.LocalDateTime;
import java.util.List;

public record CaffeineTimelineResponse(
        List<DataPoint> dataPoints,
        LocalDateTime currentTime,
        LocalDateTime bedtime,
        double targetSleepCaffeine
) {
    public record DataPoint(LocalDateTime time, double caffeineMg){}
}
