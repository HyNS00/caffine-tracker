package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.PresetBeverage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PresetBeverageJpaRepository extends JpaRepository<PresetBeverage, Long> {
}
