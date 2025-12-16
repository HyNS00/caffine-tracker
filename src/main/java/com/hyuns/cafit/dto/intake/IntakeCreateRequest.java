package com.hyuns.cafit.dto.intake;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record IntakeCreateRequest(
        @NotNull(message = "섭취 시간은 필수입니다")
        LocalDateTime consumedAt
) {}
