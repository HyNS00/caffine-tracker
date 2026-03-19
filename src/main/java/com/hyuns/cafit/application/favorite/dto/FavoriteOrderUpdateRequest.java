package com.hyuns.cafit.application.favorite.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record FavoriteOrderUpdateRequest(
        @NotEmpty(message = "즐겨찾기 목록은 필수입니다")
        List<Long> favoriteIds
) {}
