package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CustomBeverageJpaRepository extends ListCrudRepository<CustomBeverage, Long> {

    List<CustomBeverage> findByUser(User user);
}
