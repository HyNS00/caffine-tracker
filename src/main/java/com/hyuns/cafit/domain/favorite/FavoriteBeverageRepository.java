package com.hyuns.cafit.domain.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteBeverageRepository extends CrudRepository<FavoriteBeverage, Long> {
    List<FavoriteBeverage> findByUserOrderByDisplayOrderAsc(User user);

    boolean existsByUserAndPresetBeverage(User user, PresetBeverage presetBeverage);

    boolean existsByUserAndCustomBeverage(User user, CustomBeverage customBeverage);

    @Query("SELECT COALESCE(MAX(f.displayOrder),0) FROM FavoriteBeverage f WHERE f.user = :user")
    int findMaxOrderByUser(@Param("user") User user);

    @Query("SELECT f FROM FavoriteBeverage f " +
            "LEFT JOIN FETCH f.presetBeverage " +
            "LEFT JOIN FETCH f.customBeverage " +
            "WHERE f.user = :user " +
            "ORDER BY f.displayOrder ASC")
    List<FavoriteBeverage> findByUserWithBeverages(@Param("user") User user);
}
