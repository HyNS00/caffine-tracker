package com.hyuns.cafit.domain.beverage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PresetBeverageRepository extends JpaRepository<PresetBeverage,Long> {
    @Query("SELECT p FROM PresetBeverage p WHERE p.name LIKE %:keyword% OR p.brandName LIKE %:keyword%")
    List<PresetBeverage> searchPresetBeverageByKeyword(@Param("keyword") String keyword);
}
