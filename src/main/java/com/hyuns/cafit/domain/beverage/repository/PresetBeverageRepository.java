package com.hyuns.cafit.domain.beverage.repository;

import com.hyuns.cafit.domain.beverage.PresetBeverage;

import java.util.List;
import java.util.Optional;

public interface PresetBeverageRepository {

    Optional<PresetBeverage> findById(Long id);

    List<PresetBeverage> findAll();

    List<PresetBeverage> searchByKeyword(String keyword);
}
