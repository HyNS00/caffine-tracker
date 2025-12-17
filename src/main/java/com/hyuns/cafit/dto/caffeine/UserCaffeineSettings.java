package com.hyuns.cafit.dto.caffeine;

import com.hyuns.cafit.domain.user.User;

import java.time.LocalTime;

public record UserCaffeineSettings(
        double dailyLimitMg,
        double targetSleepCaffeineMg,
        double halfLifeHours,
        LocalTime bedTime
) {
    public static UserCaffeineSettings from(User user) {
        return new UserCaffeineSettings(
                user.getDailyCaffeineLimit(),
                user.getTargetSleepCaffeine(),
                user.getCaffeineHalfLife(),
                user.getBedTime()
        );
    }
}
