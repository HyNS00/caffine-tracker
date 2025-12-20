package com.hyuns.cafit.domain.intake;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.statistics.TopBeverageStat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CaffeineIntakeRepository extends CrudRepository<CaffeineIntake, Long> {
    List<CaffeineIntake> findByUserAndConsumedAtBetweenOrderByConsumedAtDesc(
            User user,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query("""
    SELECT new com.hyuns.cafit.dto.statistics.TopBeverageStat(
        i.beverageName, i.brandName, i.volumeMl, COUNT(i)
    )
    FROM CaffeineIntake i
    WHERE i.user = :user
      AND i.consumedAt BETWEEN :start AND :end
    GROUP BY i.beverageName, i.brandName, i.volumeMl
    ORDER BY COUNT(i) DESC
    """)
    List<TopBeverageStat> findTopBeverages(
            @Param("user") User user,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable
    );
}
