package com.hyuns.cafit.application.caffeine;

import com.hyuns.cafit.application.beverage.CustomBeverageService;
import com.hyuns.cafit.application.beverage.PresetBeverageService;
import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.application.beverage.dto.BeverageInfo;
import com.hyuns.cafit.application.caffeine.dto.CurrentCaffeineResponse;
import com.hyuns.cafit.application.caffeine.dto.DrinkCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CaffeineCheckFacade {

    private final CaffeineCheckService caffeineCheckService;
    private final PresetBeverageService presetBeverageService;
    private final CustomBeverageService customBeverageService;

    public CurrentCaffeineResponse getCurrentStatus(User user) {
        return caffeineCheckService.getCurrentStatus(user);
    }

    public DrinkCheckResponse checkPresetBeverage(User user, Long beverageId) {
        PresetBeverage beverage = presetBeverageService.getById(beverageId);
        return caffeineCheckService.checkBeverage(user, BeverageInfo.from(beverage));
    }

    public DrinkCheckResponse checkCustomBeverage(User user, Long beverageId) {
        CustomBeverage beverage = customBeverageService.getById(beverageId);
        return caffeineCheckService.checkBeverage(user, BeverageInfo.from(beverage));
    }
}
