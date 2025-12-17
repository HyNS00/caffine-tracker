package com.hyuns.cafit.dto.statistics;

import java.util.List;

public record DailyStatisticsResponse(
        StatisticsPeriod period,
        List<DailyStat> dailyStats,
        double periodAverage,
        double dailyLimit
) {
}
