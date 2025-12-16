package com.hyuns.cafit.dto.beverage;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import jakarta.validation.constraints.*;

public record CustomBeverageCreateRequest(

        @NotBlank(message = "음료 이름은 필수입니다")
        @Size(max = 50, message = "음료 이름은 50자 이하여야 합니다")
        String name,

        @NotNull(message = "카테고리는 필수입니다")
        BeverageCategory category,

        @NotNull(message = "용량은 필수입니다")
        @Positive(message = "용량은 0보다 커야 합니다")
        @Max(value = 10000, message = "용량은 10000ml 이하여야 합니다")
        Integer volumeMl,

        @NotNull(message = "카페인량은 필수입니다")
        @Positive(message = "카페인량은 0보다 커야 합니다")
        Double caffeineMg
) {}
