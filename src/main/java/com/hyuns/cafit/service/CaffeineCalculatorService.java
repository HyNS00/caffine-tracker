package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.intake.CaffeineIntakeRepository;
import com.hyuns.cafit.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaffeineCalculatorService {
    private final CaffeineIntakeRepository caffeineIntakeRepository;

    public double calculateRemainingCaffeine(double initialMg, double hoursElapsed, double halfLifeHours) {
        if (hoursElapsed <= 0) {
            return initialMg;
        }
        return initialMg * Math.pow(0.5, hoursElapsed / halfLifeHours);
    }

    // 전체 24시간 시점
    public double getCurrentCaffeineLevel(User user) {
        return getCaffeineLevelAt(user, LocalDateTime.now());
    }

    // 특정 시간
    public double getCaffeineLevelAt(User user, LocalDateTime targetTime) {
        // 최근 24시간 섭취 기록 조회 (반감기 5시간 기준, 24시간이면 거의 소멸)
        LocalDateTime startTime = targetTime.minusHours(24);
        List<CaffeineIntake> intakes = caffeineIntakeRepository
                .findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startTime, targetTime);

        double totalCaffeine = 0.0;
        double halfLife = user.getCaffeineHalfLife();

        for (CaffeineIntake intake : intakes) {
            double hoursElapsed = ChronoUnit.MINUTES.between(intake.getConsumedAt(), targetTime) / 60.0;
            double remaining = calculateRemainingCaffeine(intake.getCaffeineMg(), hoursElapsed, halfLife);
            totalCaffeine += remaining;
        }

        return totalCaffeine;
    }

    public double predictCaffeineAtBedtime(User user, double additionalCaffeine) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bedtime = getBedtimeToday(user, now);

        // 현재 체내 카페인이 취침시간까지 감소한 양
        double currentLevel = getCurrentCaffeineLevel(user);
        double hoursUntilBed = ChronoUnit.MINUTES.between(now, bedtime) / 60.0;

        double currentAtBedtime = calculateRemainingCaffeine(currentLevel, hoursUntilBed, user.getCaffeineHalfLife());

        // 추가 섭취분이 취침시간까지 감소한 양
        double additionalAtBedtime = calculateRemainingCaffeine(additionalCaffeine, hoursUntilBed, user.getCaffeineHalfLife());

        return currentAtBedtime + additionalAtBedtime;
    }

    private LocalDateTime getBedtimeToday(User user, LocalDateTime now) {
        LocalDateTime bedtimeToday = now.toLocalDate().atTime(user.getBedTime());

        // 이미 취침 시간이 지났으면 내일 취침 시간
        if (now.isAfter(bedtimeToday)) {
            bedtimeToday = bedtimeToday.plusDays(1);
        }

        return bedtimeToday;
    }

    // 음료를 마셔도 되는지 판단하기
    public boolean isSafeToDrink(User user, double caffeineMg) {
        double predictedAtBedtime = predictCaffeineAtBedtime(user, caffeineMg);
        return predictedAtBedtime <= user.getTargetSleepCaffeine();
    }

    public double getHoursUntilBedtime(User user) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime bedtime = getBedtimeToday(user, now);
        return ChronoUnit.MINUTES.between(now, bedtime) / 60.0;
    }
}
