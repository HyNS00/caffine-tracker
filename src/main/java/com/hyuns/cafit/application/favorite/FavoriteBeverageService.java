package com.hyuns.cafit.application.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.favorite.repository.FavoriteBeverageRepository;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.favorite.FavoriteBeverageResponse;
import com.hyuns.cafit.global.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteBeverageService {

    private final FavoriteBeverageRepository favoriteRepository;

    @Transactional(readOnly = true)
    public List<FavoriteBeverageResponse> getFavorites(User user) {
        return favoriteRepository.findByUserWithBeverages(user).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void deleteFavorite(Long favoriteId, User user) {
        FavoriteBeverage favorite = favoriteRepository.findById(favoriteId)
                .orElseThrow(FavoriteNotFoundException::new);

        if (!favorite.isOwnedBy(user)) {
            throw new FavoriteAccessDeniedException();
        }

        favoriteRepository.delete(favorite);
    }

    @Transactional
    public void updateOrder(User user, List<Long> favoriteIds) {
        Map<Long, FavoriteBeverage> favoriteMap = favoriteRepository.findByUserWithBeverages(user)
                .stream()
                .collect(Collectors.toMap(FavoriteBeverage::getId, f -> f));

        if (favoriteIds.size() != favoriteMap.size()) {
            throw new InvalidFavoriteListException();
        }

        for (int i = 0; i < favoriteIds.size(); i++) {
            Long id = favoriteIds.get(i);
            FavoriteBeverage favorite = favoriteMap.get(id);

            if (favorite == null) {
                throw new FavoriteNotFoundException();
            }

            favorite.updateOrder(i + 1);
        }
    }

    @Transactional
    public FavoriteBeverageResponse addPresetFavorite(User user, PresetBeverage beverage) {
        validateNotDuplicatePreset(user, beverage);

        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(
                user,
                beverage,
                getNextOrder(user)
        );

        return toResponse(favoriteRepository.save(favorite));
    }

    @Transactional
    public FavoriteBeverageResponse addCustomFavorite(User user, CustomBeverage beverage) {
        validateNotDuplicateCustom(user, beverage);

        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(
                user,
                beverage,
                getNextOrder(user)
        );

        return toResponse(favoriteRepository.save(favorite));
    }

    private int getNextOrder(User user) {
        return favoriteRepository.findMaxOrderByUser(user) + 1;
    }

    private void validateNotDuplicatePreset(User user, PresetBeverage beverage) {
        if (favoriteRepository.existsByUserAndPresetBeverage(user, beverage)) {
            throw new DuplicateFavoriteException();
        }
    }

    private void validateNotDuplicateCustom(User user, CustomBeverage beverage) {
        if (favoriteRepository.existsByUserAndCustomBeverage(user, beverage)) {
            throw new DuplicateFavoriteException();
        }
    }

    private FavoriteBeverageResponse toResponse(FavoriteBeverage favorite) {
        if (favorite.isPreset()) {
            return FavoriteBeverageResponse.fromPreset(favorite, favorite.getPresetBeverage());
        }
        return FavoriteBeverageResponse.fromCustom(favorite, favorite.getCustomBeverage());
    }
}
