package com.hyuns.cafit.infrastructure.beverage.persistence;

import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hyuns.cafit.domain.beverage.QPresetBeverage.presetBeverage;

@Repository
@RequiredArgsConstructor
public class PresetBeverageQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<PresetBeverage> searchByKeyword(String keyword) {
        return queryFactory
                .selectFrom(presetBeverage)
                .where(
                        presetBeverage.name.contains(keyword)
                                .or(presetBeverage.brandName.contains(keyword))
                )
                .fetch();
    }
}
