package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.repository.CustomBeverageRepository;
import com.hyuns.cafit.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CustomBeverageRepositoryAdapter implements CustomBeverageRepository {

    private final CustomBeverageJpaRepository customBeverageJpaRepository;

    @Override
    public Optional<CustomBeverage> findById(Long id) {
        return customBeverageJpaRepository.findById(id);
    }

    @Override
    public List<CustomBeverage> findByUser(User user) {
        return customBeverageJpaRepository.findByUser(user);
    }

    @Override
    public CustomBeverage save(CustomBeverage customBeverage) {
        return customBeverageJpaRepository.save(customBeverage);
    }

    @Override
    public void delete(CustomBeverage customBeverage) {
        customBeverageJpaRepository.delete(customBeverage);
    }
}
