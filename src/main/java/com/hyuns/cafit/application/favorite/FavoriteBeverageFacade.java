package com.hyuns.cafit.application.favorite;

import com.hyuns.cafit.application.beverage.CustomBeverageService;
import com.hyuns.cafit.application.beverage.PresetBeverageService;
import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.application.favorite.dto.FavoriteBeverageResponse;
import com.hyuns.cafit.application.favorite.dto.FavoriteCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FavoriteBeverageFacade {

    private final FavoriteBeverageService favoriteBeverageService;
    private final PresetBeverageService presetBeverageService;
    private final CustomBeverageService customBeverageService;

    @Transactional
    public FavoriteBeverageResponse addFavorite(User user, FavoriteCreateRequest request) {
        if (request.type().isPreset()) {
            PresetBeverage beverage = presetBeverageService.getById(request.beverageId());
            return favoriteBeverageService.addPresetFavorite(user, beverage);
        }
        CustomBeverage beverage = customBeverageService.getByIdAndValidateOwnership(request.beverageId(), user);
        return favoriteBeverageService.addCustomFavorite(user, beverage);
    }

    public List<FavoriteBeverageResponse> getFavorites(User user) {
        return favoriteBeverageService.getFavorites(user);
    }

    @Transactional
    public void deleteFavorite(Long favoriteId, User user) {
        favoriteBeverageService.deleteFavorite(favoriteId, user);
    }

    @Transactional
    public void updateOrder(User user, List<Long> favoriteIds) {
        favoriteBeverageService.updateOrder(user, favoriteIds);
    }
}
