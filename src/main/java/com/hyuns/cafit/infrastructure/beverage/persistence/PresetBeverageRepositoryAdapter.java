package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.beverage.repository.PresetBeverageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PresetBeverageRepositoryAdapter implements PresetBeverageRepository {

    private final PresetBeverageJpaRepository presetBeverageJpaRepository;
    private final PresetBeverageQueryRepository presetBeverageQueryRepository;

    @Override
    public Optional<PresetBeverage> findById(Long id) {
        return presetBeverageJpaRepository.findById(id);
    }

    @Override
    public List<PresetBeverage> findAll() {
        return presetBeverageJpaRepository.findAll();
    }

    @Override
    public List<PresetBeverage> searchByKeyword(String keyword) {
        return presetBeverageQueryRepository.searchByKeyword(keyword);
    }
}
