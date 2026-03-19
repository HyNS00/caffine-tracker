package com.hyuns.cafit.application.intake.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CaffeineIntakeCreateRequest(
        @NotNull(message = "섭취 시간은 필수입니다")
        LocalDateTime consumedAt
) {}
