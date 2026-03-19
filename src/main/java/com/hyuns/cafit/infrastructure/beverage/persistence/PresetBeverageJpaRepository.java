package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.PresetBeverage;
import org.springframework.data.repository.ListCrudRepository;

public interface PresetBeverageJpaRepository extends ListCrudRepository<PresetBeverage, Long> {
}
