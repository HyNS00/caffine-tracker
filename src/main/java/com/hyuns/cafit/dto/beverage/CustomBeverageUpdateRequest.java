package com.hyuns.cafit.dto.beverage;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CustomBeverageUpdateRequest(
        @NotBlank(message = "음료 이름은 필수입니다")
        String name,

        @NotNull(message = "카테고리는 필수입니다")
        String category,

        @NotNull(message = "용량은 필수입니다")
        @Positive(message = "용량은 0보다 커야 합니다")
        Integer volumeMl,

        @NotNull(message = "카페인량은 필수입니다")
        @Positive(message = "카페인량은 0보다 커야 합니다")
        Double caffeineMg
) {
}
