package com.hyuns.cafit.domain.intake.repository;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CaffeineIntakeRepository {

    Optional<CaffeineIntake> findById(Long id);

    List<CaffeineIntake> findByUserAndConsumedAtBetween(User user, LocalDateTime start, LocalDateTime end);

    CaffeineIntake save(CaffeineIntake caffeineIntake);

    void delete(CaffeineIntake caffeineIntake);
}
