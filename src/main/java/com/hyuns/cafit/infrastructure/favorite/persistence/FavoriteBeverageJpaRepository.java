package com.hyuns.cafit.infrastructure.favorite.persistence;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

public interface FavoriteBeverageJpaRepository extends CrudRepository<FavoriteBeverage, Long> {

    boolean existsByUserAndPresetBeverage(User user, PresetBeverage presetBeverage);

    boolean existsByUserAndCustomBeverage(User user, CustomBeverage customBeverage);
}
