package com.hyuns.cafit.infrastructure.favorite.persistence;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.favorite.repository.FavoriteBeverageRepository;
import com.hyuns.cafit.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FavoriteBeverageRepositoryAdapter implements FavoriteBeverageRepository {

    private final FavoriteBeverageJpaRepository favoriteBeverageJpaRepository;
    private final FavoriteBeverageQueryRepository favoriteBeverageQueryRepository;

    @Override
    public Optional<FavoriteBeverage> findById(Long id) {
        return favoriteBeverageJpaRepository.findById(id);
    }

    @Override
    public List<FavoriteBeverage> findByUserWithBeverages(User user) {
        return favoriteBeverageQueryRepository.findByUserWithBeverages(user);
    }

    @Override
    public boolean existsByUserAndPresetBeverage(User user, PresetBeverage presetBeverage) {
        return favoriteBeverageJpaRepository.existsByUserAndPresetBeverage(user, presetBeverage);
    }

    @Override
    public boolean existsByUserAndCustomBeverage(User user, CustomBeverage customBeverage) {
        return favoriteBeverageJpaRepository.existsByUserAndCustomBeverage(user, customBeverage);
    }

    @Override
    public int findMaxOrderByUser(User user) {
        return favoriteBeverageQueryRepository.findMaxOrderByUser(user);
    }

    @Override
    public FavoriteBeverage save(FavoriteBeverage favoriteBeverage) {
        return favoriteBeverageJpaRepository.save(favoriteBeverage);
    }

    @Override
    public void delete(FavoriteBeverage favoriteBeverage) {
        favoriteBeverageJpaRepository.delete(favoriteBeverage);
    }
}
