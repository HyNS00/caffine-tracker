package com.hyuns.cafit.dto.intake;

import com.hyuns.cafit.domain.intake.CaffeineIntake;

import java.time.LocalDateTime;

public record IntakeResponse(
        Long id,
        String beverageName,
        String brandName,
        String category,
        int volumeMl,
        double caffeineMg,
        LocalDateTime consumedAt,
        String displayName
) {
    public static IntakeResponse from(CaffeineIntake intake) {
        return new IntakeResponse(
                intake.getId(),
                intake.getBeverageName(),
                intake.getBrandName(),
                intake.getCategory().getDisplayName(),
                intake.getVolumeMl(),
                intake.getCaffeineMg(),
                intake.getConsumedAt(),
                intake.getDisplayName()
        );
    }
}
