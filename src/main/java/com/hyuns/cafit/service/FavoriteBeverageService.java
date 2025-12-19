package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.beverage.*;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverageRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.favorite.FavoriteBeverageResponse;
import com.hyuns.cafit.dto.favorite.FavoriteCreateRequest;
import com.hyuns.cafit.errors.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteBeverageService {
    private final Clock clock;
    private final FavoriteBeverageRepository favoriteRepository;
    private final PresetBeverageService presetBeverageService;
    private final CustomBeverageService customBeverageService;

    @Transactional
    public FavoriteBeverageResponse addFavorite(User user, FavoriteCreateRequest request) {
        if (request.type() == BeverageType.PRESET) {
            return addPresetFavorite(user, request.beverageId());
        }
        return addCustomFavorite(user, request.beverageId());
    }

    @Transactional(readOnly = true)
    public List<FavoriteBeverageResponse> getFavorites(User user) {
        return favoriteRepository.findByUserWithBeverages(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteFavorite(Long favoriteId, User user) {
        FavoriteBeverage favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessage.FAVORITE_NOT_FOUND));

        if (!favorite.isOwnedBy(user)) {
            throw new ForbiddenException(ErrorMessage.UNAUTHORIZED_FAVORITE_ACCESS);
        }

        favoriteRepository.delete(favorite);
    }

    @Transactional
    public void updateOrder(User user, List<Long> favoriteIds) {
        Map<Long, FavoriteBeverage> favoriteMap = favoriteRepository.findByUserWithBeverages(user)
                .stream()
                .collect(Collectors.toMap(FavoriteBeverage::getId, f -> f));

        if (favoriteIds.size() != favoriteMap.size()) {
            throw new BadRequestException(ErrorMessage.INVALID_FAVORITE_LIST);
        }

        for (int i = 0; i < favoriteIds.size(); i++) {
            Long id = favoriteIds.get(i);
            FavoriteBeverage favorite = favoriteMap.get(id);

            if (favorite == null) {
                throw new EntityNotFoundException(ErrorMessage.FAVORITE_NOT_FOUND);
            }

            favorite.updateOrder(i + 1);
        }
    }

    private FavoriteBeverageResponse addPresetFavorite(User user, Long beverageId) {
        PresetBeverage beverage = presetBeverageService.getById(beverageId);

        validateNotDuplicatePreset(user, beverage);

        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(
                user,
                beverage,
                getNextOrder(user),
                LocalDateTime.now(clock)
        );

        return toResponse(favoriteRepository.save(favorite));
    }

    private FavoriteBeverageResponse addCustomFavorite(User user, Long beverageId) {
        CustomBeverage beverage = customBeverageService.getByIdAndValidateOwnership(beverageId, user);

        validateNotDuplicateCustom(user, beverage);

        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(
                user,
                beverage,
                getNextOrder(user),
                LocalDateTime.now(clock)
        );

        return toResponse(favoriteRepository.save(favorite));
    }

    private int getNextOrder(User user) {
        return favoriteRepository.findMaxOrderByUser(user) + 1;
    }

    private void validateNotDuplicatePreset(User user, PresetBeverage beverage) {
        if (favoriteRepository.existsByUserAndPresetBeverage(user, beverage)) {
            throw new DuplicateException(ErrorMessage.DUPLICATE_FAVORITE);
        }
    }

    private void validateNotDuplicateCustom(User user, CustomBeverage beverage) {
        if (favoriteRepository.existsByUserAndCustomBeverage(user, beverage)) {
            throw new DuplicateException(ErrorMessage.DUPLICATE_FAVORITE);
        }
    }

    private FavoriteBeverageResponse toResponse(FavoriteBeverage favorite) {
        if (favorite.isPreset()) {
            return FavoriteBeverageResponse.fromPreset(favorite, favorite.getPresetBeverage());
        }
        return FavoriteBeverageResponse.fromCustom(favorite, favorite.getCustomBeverage());
    }
}