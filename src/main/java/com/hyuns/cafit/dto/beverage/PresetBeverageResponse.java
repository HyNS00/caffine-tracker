package com.hyuns.cafit.dto.beverage;

import com.hyuns.cafit.domain.beverage.PresetBeverage;

public record PresetBeverageResponse(
        Long id,
        String name,
        String brandName,
        String category,
        int volumeMl,
        double caffeineMg,
        String displayName
) {
    public static PresetBeverageResponse from(PresetBeverage beverage) {
        return new PresetBeverageResponse(
                beverage.getId(),
                beverage.getName(),
                beverage.getBrandName(),
                beverage.getCategory().getDisplayName(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                beverage.getDisplayName()
        );
    }
}
