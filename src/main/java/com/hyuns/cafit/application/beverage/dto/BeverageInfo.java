package com.hyuns.cafit.application.beverage.dto;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;

public record BeverageInfo(
        String name,
        double caffeineMg
) {
    public static BeverageInfo from(PresetBeverage beverage) {
        return new BeverageInfo(
                beverage.getDisplayName(),
                beverage.getCaffeineMg()
        );
    }

    public static BeverageInfo from(CustomBeverage beverage) {
        return new BeverageInfo(
                beverage.getDisplayName(),
                beverage.getCaffeineMg()
        );
    }
}
