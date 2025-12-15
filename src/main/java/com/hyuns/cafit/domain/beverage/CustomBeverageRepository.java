package com.hyuns.cafit.domain.beverage;

import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CustomBeverageRepository extends CrudRepository<CustomBeverage, Long> {
    List<CustomBeverage> findByUser(User user);
}
