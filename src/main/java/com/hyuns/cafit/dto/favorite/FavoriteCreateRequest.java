package com.hyuns.cafit.dto.favorite;

import com.hyuns.cafit.domain.beverage.BeverageType;
import jakarta.validation.constraints.NotNull;

public record FavoriteCreateRequest(
        @NotNull(message = "음료 카테고리는 필수입니다")
        BeverageType type,

        @NotNull(message = "음료 ID는 필수입니다")
        Long beverageId
) {}
