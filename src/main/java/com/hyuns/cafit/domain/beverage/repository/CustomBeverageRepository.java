package com.hyuns.cafit.domain.beverage.repository;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface CustomBeverageRepository {

    Optional<CustomBeverage> findById(Long id);

    List<CustomBeverage> findByUser(User user);

    CustomBeverage save(CustomBeverage customBeverage);

    void delete(CustomBeverage customBeverage);
}
