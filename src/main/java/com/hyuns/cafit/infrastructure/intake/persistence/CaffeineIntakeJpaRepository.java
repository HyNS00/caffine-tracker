package com.hyuns.cafit.infrastructure.intake.persistence;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakeJpaRepository extends CrudRepository<CaffeineIntake, Long> {

    List<CaffeineIntake> findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(
            User user, LocalDateTime start, LocalDateTime end
    );
}
