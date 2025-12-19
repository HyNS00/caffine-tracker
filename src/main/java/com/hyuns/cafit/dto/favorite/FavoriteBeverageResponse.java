package com.hyuns.cafit.dto.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;

public record FavoriteBeverageResponse(
        Long id,
        String type,
        Long beverageId,
        String name,
        String brandName,
        String category,
        int volumeMl,
        double caffeineMg,
        int displayOrder
) {
    public static FavoriteBeverageResponse fromPreset(FavoriteBeverage favorite, PresetBeverage beverage) {
        return new FavoriteBeverageResponse(
                favorite.getId(),
                "PRESET",
                beverage.getId(),
                beverage.getName(),
                beverage.getBrandName(),
                beverage.getCategory().getDisplayName(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                favorite.getDisplayOrder()
        );
    }

    public static FavoriteBeverageResponse fromCustom(FavoriteBeverage favorite, CustomBeverage beverage) {
        return new FavoriteBeverageResponse(
                favorite.getId(),
                "CUSTOM",
                beverage.getId(),
                beverage.getName(),
                null,
                beverage.getCategory().getDisplayName(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                favorite.getDisplayOrder()
        );
    }
}
