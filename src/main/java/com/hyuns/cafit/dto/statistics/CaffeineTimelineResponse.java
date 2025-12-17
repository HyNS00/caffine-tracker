package com.hyuns.cafit.dto.statistics;

import java.time.LocalDateTime;
import java.util.List;

public record CaffeineTimelineResponse(
        List<TimelineDataPoint> dataPoints,
        LocalDateTime currentTime,
        LocalDateTime bedtime,
        double targetSleepCaffeine
) {}
