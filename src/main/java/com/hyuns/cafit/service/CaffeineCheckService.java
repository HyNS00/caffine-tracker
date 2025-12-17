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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaffeineCheckService {
    private final CaffeineCalculatorService calculatorService;
    private final CaffeineIntakeRepository intakeRepository;
    private final PresetBeverageRepository presetBeverageRepository;
    private final CustomBeverageRepository customBeverageRepository;

    public  CurrentCaffeineResponse getCurrentStatus(User user) {
        // 1. 계산
        double currentMg = calculatorService.getCurrentCaffeineLevel(user);
        double predictedAtBedtimeMg = calculatorService.predictCaffeineAtBedtime(user, 0);
        double todayTotalMg = getTodayTotalIntake(user);
        double hoursUntilBedtime = calculatorService.getHoursUntilBedtime(user);

        // 2. 각 객체 생성
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

        // 3. 조립
        return new CurrentCaffeineResponse(status, settings, recommendation);
    }

    public DrinkCheckResponse checkPresetBeverage(User user, Long beverageId) {
        PresetBeverage beverage = presetBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.BEVERAGE_NOT_FOUND));

        BeverageInfo beverageInfo = BeverageInfo.from(beverage);
        return buildDrinkCheckResponse(user, beverageInfo);
    }

    public DrinkCheckResponse checkCustomBeverage(User user, Long beverageId) {
        CustomBeverage beverage = customBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.CUSTOM_BEVERAGE_NOT_FOUND));

        BeverageInfo beverageInfo = BeverageInfo.from(beverage);
        return buildDrinkCheckResponse(user, beverageInfo);
    }

    private DrinkCheckResponse buildDrinkCheckResponse(User user, BeverageInfo beverageInfo) {
        // 1. 계산
        double currentMg = calculatorService.getCurrentCaffeineLevel(user);
        double predictedAtBedtimeMg = calculatorService.predictCaffeineAtBedtime(user, 0);
        double todayTotalMg = getTodayTotalIntake(user);
        double hoursUntilBedtime = calculatorService.getHoursUntilBedtime(user);

        // 2. before 상태
        CaffeineStatus before = new CaffeineStatus(
                round(currentMg),
                round(predictedAtBedtimeMg),
                round(todayTotalMg),
                round(hoursUntilBedtime)
        );

        // 3. after 상태 (음료 추가 후)
        double afterCurrentMg = currentMg + beverageInfo.caffeineMg();
        double afterPredictedMg = calculatorService.predictCaffeineAtBedtime(user, beverageInfo.caffeineMg());
        double afterTodayTotalMg = todayTotalMg + beverageInfo.caffeineMg();

        CaffeineStatus after = new CaffeineStatus(
                round(afterCurrentMg),
                round(afterPredictedMg),
                round(afterTodayTotalMg),
                round(hoursUntilBedtime)
        );

        // 4. 설정값
        UserCaffeineSettings settings = UserCaffeineSettings.from(user);

        // 5. 판단 (after 기준)
        DrinkRecommendation recommendation = DrinkRecommendation.determine(
                afterTodayTotalMg,
                user.getDailyCaffeineLimit(),
                afterPredictedMg,
                user.getTargetSleepCaffeine()
        );

        // 6. 조립
        return new DrinkCheckResponse(
                beverageInfo,
                before,
                after,
                settings,
                recommendation,
                recommendation == DrinkRecommendation.SAFE
        );
    }

    private double getTodayTotalIntake(User user) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<CaffeineIntake> todayIntakes = intakeRepository
                .findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, startOfDay, endOfDay);

        return todayIntakes.stream()
                .mapToDouble(CaffeineIntake::getCaffeineMg)
                .sum();
    }

    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }
}
