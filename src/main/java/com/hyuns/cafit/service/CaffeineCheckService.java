package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.CustomBeverageRepository;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverageRepository;
import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.intake.CaffeineIntakeRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.beverage.BeverageInfo;
import com.hyuns.cafit.dto.caffeine.*;
import com.hyuns.cafit.errors.EntityNotFoundException;
import com.hyuns.cafit.errors.ErrorMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.hyuns.cafit.util.NumberUtils.round;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaffeineCheckService {
    private final Clock clock;
    private final CaffeineDecayCalculator decayCalculator;
    private final CaffeineIntakeRepository intakeRepository;
    private final PresetBeverageRepository presetBeverageRepository;
    private final CustomBeverageRepository customBeverageRepository;


    public CurrentCaffeineResponse getCurrentStatus(User user) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<CaffeineIntake> intakes = getRecentIntakes(user, now);

        double currentMg = decayCalculator.caffeineLevelAt(intakes, now, user.getCaffeineHalfLife());
        double predictedAtBedtimeMg = calculatePredictedAtBedtime(user, intakes, now, 0);
        double todayTotalMg = getTodayTotalIntake(user);
        double hoursUntilBedtime = calculateHoursUntilBedtime(user, now);

        CaffeineStatus status = new CaffeineStatus(
                round(currentMg),
                round(predictedAtBedtimeMg),
                round(todayTotalMg),
                round(hoursUntilBedtime)
        );

        UserCaffeineSettings settings = UserCaffeineSettings.from(user);

        DrinkRecommendation recommendation = DrinkRecommendation.determine(
                todayTotalMg,
                user.getDailyCaffeineLimit(),
                predictedAtBedtimeMg,
                user.getTargetSleepCaffeine()
        );

        return new CurrentCaffeineResponse(status, settings, recommendation);
    }


    public DrinkCheckResponse checkPresetBeverage(User user, Long beverageId) {
        PresetBeverage beverage = presetBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.BEVERAGE_NOT_FOUND));

        return buildDrinkCheckResponse(user, BeverageInfo.from(beverage));
    }

    public DrinkCheckResponse checkCustomBeverage(User user, Long beverageId) {
        CustomBeverage beverage = customBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.CUSTOM_BEVERAGE_NOT_FOUND));

        return buildDrinkCheckResponse(user, BeverageInfo.from(beverage));
    }

    private DrinkCheckResponse buildDrinkCheckResponse(User user, BeverageInfo beverageInfo) {
        LocalDateTime now = LocalDateTime.now(clock);
        List<CaffeineIntake> intakes = getRecentIntakes(user, now);
        double todayTotalMg = getTodayTotalIntake(user);

        CaffeineStatus before = buildCaffeineStatus(user, intakes, now, todayTotalMg, 0);
        CaffeineStatus after = buildCaffeineStatus(user, intakes, now, todayTotalMg, beverageInfo.caffeineMg());

        UserCaffeineSettings settings = UserCaffeineSettings.from(user);
        DrinkRecommendation recommendation = determineRecommendation(user, after);

        return new DrinkCheckResponse(
                beverageInfo,
                before,
                after,
                settings,
                recommendation,
                recommendation == DrinkRecommendation.SAFE
        );
    }

    private CaffeineStatus buildCaffeineStatus(
            User user,
            List<CaffeineIntake> intakes,
            LocalDateTime now,
            double todayTotalMg,
            double additionalCaffeine
    ) {

        double currentMg = decayCalculator.caffeineLevelAt(intakes, now, user.getCaffeineHalfLife()) + additionalCaffeine;
        double predictedAtBedtimeMg = calculatePredictedAtBedtime(user, intakes, now, additionalCaffeine);
        double totalMg = todayTotalMg + additionalCaffeine;
        double hoursUntilBedtime = calculateHoursUntilBedtime(user, now);

        return new CaffeineStatus(
                round(currentMg),
                round(predictedAtBedtimeMg),
                round(totalMg),
                round(hoursUntilBedtime)
        );
    }

    private DrinkRecommendation determineRecommendation(User user, CaffeineStatus status) {
        return DrinkRecommendation.determine(
                status.todayTotalMg(),
                user.getDailyCaffeineLimit(),
                status.predictedAtBedtimeMg(),
                user.getTargetSleepCaffeine()
        );
    }

    private List<CaffeineIntake> getRecentIntakes(User user, LocalDateTime now) {
        LocalDateTime startTime = now.minusHours(24);
        return intakeRepository.findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startTime, now);
    }

    private double calculatePredictedAtBedtime(
            User user,
            List<CaffeineIntake> intakes,
            LocalDateTime now,
            double additionalCaffeine
    ) {
        LocalDateTime bedtime = calculateBedtime(user, now);
        double hoursUntilBed = ChronoUnit.MINUTES.between(now, bedtime) / 60.0;

        double currentAtBedtime = decayCalculator.caffeineLevelAt(intakes, bedtime, user.getCaffeineHalfLife());
        double additionalAtBedtime = decayCalculator.calculateRemaining(additionalCaffeine, hoursUntilBed, user.getCaffeineHalfLife());

        return currentAtBedtime + additionalAtBedtime;
    }

    private LocalDateTime calculateBedtime(User user, LocalDateTime now) {
        LocalDateTime bedtime = now.toLocalDate().atTime(user.getBedTime());

        if (now.isAfter(bedtime)) {
            bedtime = bedtime.plusDays(1);
        }

        return bedtime;
    }

    private double calculateHoursUntilBedtime(User user, LocalDateTime now) {
        LocalDateTime bedtime = calculateBedtime(user, now);
        return ChronoUnit.MINUTES.between(now, bedtime) / 60.0;
    }

    private double getTodayTotalIntake(User user) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<CaffeineIntake> todayIntakes = intakeRepository
                .findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startOfDay, endOfDay);

        return todayIntakes.stream()
                .mapToDouble(CaffeineIntake::getCaffeineMg)
                .sum();
    }
}