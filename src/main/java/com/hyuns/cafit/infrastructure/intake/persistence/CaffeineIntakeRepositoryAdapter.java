package com.hyuns.cafit.infrastructure.intake.persistence;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.intake.repository.CaffeineIntakeRepository;
import com.hyuns.cafit.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CaffeineIntakeRepositoryAdapter implements CaffeineIntakeRepository {

    private final CaffeineIntakeJpaRepository caffeineIntakeJpaRepository;

    @Override
    public Optional<CaffeineIntake> findById(Long id) {
        return caffeineIntakeJpaRepository.findById(id);
    }

    @Override
    public List<CaffeineIntake> findByUserAndConsumedAtBetween(User user, LocalDateTime start, LocalDateTime end) {
        return caffeineIntakeJpaRepository.findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(user, start, end);
    }

    @Override
    public CaffeineIntake save(CaffeineIntake caffeineIntake) {
        return caffeineIntakeJpaRepository.save(caffeineIntake);
    }

    @Override
    public void delete(CaffeineIntake caffeineIntake) {
        caffeineIntakeJpaRepository.delete(caffeineIntake);
    }
}
