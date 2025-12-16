package com.hyuns.cafit.domain.intake;

import com.hyuns.cafit.domain.user.User;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakeRepository extends CrudRepository<CaffeineIntake, Long> {
    List<CaffeineIntake> findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(
            User user,
            LocalDateTime start,
            LocalDateTime end
    );
}
