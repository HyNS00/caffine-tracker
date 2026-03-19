package com.hyuns.cafit.infrastructure.intake.persistence;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakeJpaRepository extends ListCrudRepository<CaffeineIntake, Long> {

    List<CaffeineIntake> findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(
            User user, LocalDateTime start, LocalDateTime end
    );
}
