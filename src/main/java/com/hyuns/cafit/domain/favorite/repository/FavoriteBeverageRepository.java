package com.hyuns.cafit.domain.favorite.repository;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface FavoriteBeverageRepository {

    Optional<FavoriteBeverage> findById(Long id);

    List<FavoriteBeverage> findByUserWithBeverages(User user);

    boolean existsByUserAndPresetBeverage(User user, PresetBeverage presetBeverage);

    boolean existsByUserAndCustomBeverage(User user, CustomBeverage customBeverage);

    int findMaxOrderByUser(User user);

    FavoriteBeverage save(FavoriteBeverage favoriteBeverage);

    void delete(FavoriteBeverage favoriteBeverage);
}
