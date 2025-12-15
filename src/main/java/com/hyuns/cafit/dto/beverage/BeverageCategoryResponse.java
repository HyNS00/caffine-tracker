package com.hyuns.cafit.dto.beverage;

import com.hyuns.cafit.domain.beverage.BeverageCategory;

public record BeverageCategoryResponse(
        String code,
        String displayName,
        double caffeineMgPer100ml,
        int defaultServingSizeMl
) {
    public static BeverageCategoryResponse from(BeverageCategory category) {
        return new BeverageCategoryResponse(
                category.name(),
                category.getDisplayName(),
                category.getCaffeineMgPer100ml(),
                category.getDefaultServingSizeMl()
        );
    }
}
