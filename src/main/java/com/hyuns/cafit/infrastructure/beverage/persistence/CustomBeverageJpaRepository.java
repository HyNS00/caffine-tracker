package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomBeverageJpaRepository extends CrudRepository<CustomBeverage, Long> {

    List<CustomBeverage> findByUser(User user);
}
