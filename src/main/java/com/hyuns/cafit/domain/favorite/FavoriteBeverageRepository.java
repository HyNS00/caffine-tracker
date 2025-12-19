package com.hyuns.cafit.domain.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteBeverageRepository extends CrudRepository<FavoriteBeverage, Long> {
    List<FavoriteBeverage> findByUserOrderByDisplayOrderAsc(User user);

    Optional<FavoriteBeverage> findByUserAndPresetBeverage(User user, PresetBeverage presetBeverage);

    Optional<FavoriteBeverage> findByUserAndCustomBeverage(User user, CustomBeverage presetBeverage);

    @Query("SELECT COALESCE(MAX(f.displayOrder),0) FROM FavoriteBeverage f WHERE f.user = :user")
    int findMaxOrderByUser(@Param("user") User user);

}
