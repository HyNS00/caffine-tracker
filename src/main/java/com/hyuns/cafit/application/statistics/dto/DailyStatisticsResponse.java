package com.hyuns.cafit.application.statistics.dto;

import java.util.List;

public record DailyStatisticsResponse(
        StatisticsPeriod period,
        List<DailyStat> dailyStats,
        double periodAverage,
        double dailyLimit
) {
}
