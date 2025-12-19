package com.hyuns.cafit.dto.intake;

import com.hyuns.cafit.domain.beverage.BeverageType;
import com.hyuns.cafit.domain.intake.CaffeineIntake;

import java.time.LocalDateTime;

public record CaffeineIntakeResponse(
        Long id,
        String beverageName,
        String brandName,
        String category,
        int volumeMl,
        double caffeineMg,
        LocalDateTime consumedAt,
        String displayName,
        BeverageType sourceType,
        Long sourceBeverageId
) {
    public static CaffeineIntakeResponse from(CaffeineIntake intake) {
        return new CaffeineIntakeResponse(
                intake.getId(),
                intake.getBeverageName(),
                intake.getBrandName(),
                intake.getCategory().getDisplayName(),
                intake.getVolumeMl(),
                intake.getCaffeineMg(),
                intake.getConsumedAt(),
                intake.getDisplayName(),
                intake.getSourceType(),
                intake.getSourceBeverageId()
        );
    }
}
