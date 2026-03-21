package com.hyuns.cafit.application.beverage.dto;

import com.hyuns.cafit.domain.beverage.CustomBeverage;

public record CustomBeverageResponse(
        Long id,
        String name,
        String category,
        int volumeMl,
        double caffeineMg,
        String displayName
) {
    public static CustomBeverageResponse from(CustomBeverage beverage) {
        return new CustomBeverageResponse(
                beverage.getId(),
                beverage.getName(),
                beverage.getCategory().getDisplayName(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                beverage.getDisplayName()
        );
    }
}
