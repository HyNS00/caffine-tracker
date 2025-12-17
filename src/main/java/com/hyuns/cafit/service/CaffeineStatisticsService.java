package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.intake.CaffeineIntakeRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.statistics.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static com.hyuns.cafit.util.NumberUtils.round;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaffeineStatisticsService {
    private final Clock clock;
    private final CaffeineDecayCalculator decayCalculator;
    private final CaffeineIntakeRepository intakeRepository;

    public CaffeineTimelineResponse getTimeline(User user, int hours) {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime bedtime = calculateBedtime(user, now);

        List<CaffeineIntake> intakes = getRecentIntakes(user, now);

        List<TimelineDataPoint> dataPoints = buildTimelineDataPoints(intakes, user, now, hours);

        return new CaffeineTimelineResponse(
                dataPoints,
                now,
                bedtime,
                user.getTargetSleepCaffeine()
        );
    }

    public DailyStatisticsResponse getDailyStatistics(User user, int days) {
        LocalDate endDate = LocalDate.now(clock);
        // 8일 주의
        LocalDate startDate = endDate.minusDays(days - 1);
        List<DailyStat> dailyStats = buildDailyStats(user, startDate, days);
        double periodAverage = calculatePeriodAverage(dailyStats);

        return new DailyStatisticsResponse(
                new StatisticsPeriod(startDate, endDate),
                dailyStats,
                periodAverage,
                user.getDailyCaffeineLimit()
        );
    }


    private LocalDateTime calculateBedtime(User user, LocalDateTime now) {
        LocalDateTime bedtime = now.toLocalDate().atTime(user.getBedTime());

        if (now.isAfter(bedtime)) {
            bedtime = bedtime.plusDays(1);
        }

        return bedtime;
    }

    private List<CaffeineIntake> getRecentIntakes(User user, LocalDateTime now) {
        LocalDateTime startTime = now.minusHours(24);
        return intakeRepository.findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startTime, now);
    }

    private List<TimelineDataPoint> buildTimelineDataPoints(
            List<CaffeineIntake> intakes,
            User user,
            LocalDateTime now,
            int hours
    ) {
        List<TimelineDataPoint> dataPoints = new ArrayList<>();

        for (int i = 0; i <= hours; i++) {
            LocalDateTime targetTime = now.plusHours(i);
            double caffeineMg = decayCalculator.caffeineLevelAt(intakes, targetTime, user.getCaffeineHalfLife());
            dataPoints.add(new TimelineDataPoint(targetTime, round(caffeineMg)));
        }

        return dataPoints;
    }

    // 데일리통계
    private List<DailyStat> buildDailyStats(User user, LocalDate startDate, int days) {
        List<DailyStat> dailyStats = new ArrayList<>();

        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            dailyStats.add(buildDailyStat(user, date));
        }

        return dailyStats;
    }

    private DailyStat buildDailyStat(User user, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<CaffeineIntake> intakes = intakeRepository
                .findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startOfDay, endOfDay);

        double dailyTotal = intakes.stream()
                .mapToDouble(CaffeineIntake::getCaffeineMg)
                .sum();

        return new DailyStat(date, round(dailyTotal), intakes.size());
    }

    private double calculatePeriodAverage(List<DailyStat> dailyStats) {
        if (dailyStats.isEmpty()) {
            return 0;
        }

        double total = dailyStats.stream()
                .mapToDouble(DailyStat::totalCaffeineMg)
                .sum();

        return round(total / dailyStats.size());
    }
}
