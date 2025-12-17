package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.CustomBeverageRepository;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverageRepository;
import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.intake.CaffeineIntakeRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.intake.CaffeineIntakeCreateRequest;
import com.hyuns.cafit.dto.intake.CaffeineIntakeResponse;
import com.hyuns.cafit.errors.EntityNotFoundException;
import com.hyuns.cafit.errors.ErrorMessage;
import com.hyuns.cafit.errors.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CaffeineIntakeService {
    private final Clock clock;
    private final CaffeineIntakeRepository intakeRepository;
    private final PresetBeverageRepository presetBeverageRepository;
    private final CustomBeverageRepository customBeverageRepository;

    @Transactional
    public CaffeineIntakeResponse recordPresetIntake(
            User user,
            Long beverageId,
            CaffeineIntakeCreateRequest request
    ) {
        PresetBeverage beverage = presetBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.BEVERAGE_NOT_FOUND));

        CaffeineIntake intake = CaffeineIntake.fromPreset(user, beverage, request.consumedAt());
        CaffeineIntake saved = intakeRepository.save(intake);

        return CaffeineIntakeResponse.from(saved);
    }

    @Transactional
    public CaffeineIntakeResponse recordCustomIntake(
            User user,
            Long beverageId,
            CaffeineIntakeCreateRequest request
    ) {
        CustomBeverage beverage = customBeverageRepository.findById(beverageId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.CUSTOM_BEVERAGE_NOT_FOUND));

        if (!beverage.isOwnedBy(user)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED_BEVERAGE_ACCESS);
        }

        CaffeineIntake intake = CaffeineIntake.fromCustom(user, beverage, request.consumedAt());
        CaffeineIntake saved = intakeRepository.save(intake);

        return CaffeineIntakeResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<CaffeineIntakeResponse> getTodayIntakes(User user) {
        LocalDate today = LocalDate.now(clock);
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<CaffeineIntake> intakes = intakeRepository.findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(
                user, startOfDay, endOfDay
        );

        return intakes.stream()
                .map(CaffeineIntakeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteIntake(Long intakeId, User user) {
        CaffeineIntake intake = intakeRepository.findById(intakeId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.INTAKE_NOT_FOUND));

        if (!intake.isOwnedBy(user)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED_INTAKE_ACCESS);
        }

        intakeRepository.delete(intake);
    }

}
