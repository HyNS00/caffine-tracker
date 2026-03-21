package com.hyuns.cafit.infrastructure.intake.persistence;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.application.statistics.dto.TopBeverageStat;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.hyuns.cafit.domain.intake.QCaffeineIntake.caffeineIntake;

@Repository
@RequiredArgsConstructor
public class CaffeineIntakeQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<TopBeverageStat> findTopBeverages(User user, LocalDateTime start, LocalDateTime end, int limit) {
        return queryFactory
                .select(Projections.constructor(TopBeverageStat.class,
                        caffeineIntake.beverageName,
                        caffeineIntake.brandName,
                        caffeineIntake.volumeMl,
                        caffeineIntake.count()
                ))
                .from(caffeineIntake)
                .where(
                        caffeineIntake.user.eq(user),
                        caffeineIntake.consumedAt.between(start, end)
                )
                .groupBy(caffeineIntake.beverageName, caffeineIntake.brandName, caffeineIntake.volumeMl)
                .orderBy(caffeineIntake.count().desc())
                .limit(limit)
                .fetch();
    }
}
