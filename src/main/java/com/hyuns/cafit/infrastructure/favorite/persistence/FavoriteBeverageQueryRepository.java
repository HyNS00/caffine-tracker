package com.hyuns.cafit.infrastructure.favorite.persistence;

import com.hyuns.cafit.domain.favorite.FavoriteBeverage;
import com.hyuns.cafit.domain.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.hyuns.cafit.domain.beverage.QCustomBeverage.customBeverage;
import static com.hyuns.cafit.domain.beverage.QPresetBeverage.presetBeverage;
import static com.hyuns.cafit.domain.favorite.QFavoriteBeverage.favoriteBeverage;

@Repository
@RequiredArgsConstructor
public class FavoriteBeverageQueryRepository {

    private final JPAQueryFactory queryFactory;

    public List<FavoriteBeverage> findByUserWithBeverages(User user) {
        return queryFactory
                .selectFrom(favoriteBeverage)
                .leftJoin(favoriteBeverage.presetBeverage, presetBeverage).fetchJoin()
                .leftJoin(favoriteBeverage.customBeverage, customBeverage).fetchJoin()
                .where(favoriteBeverage.user.eq(user))
                .orderBy(favoriteBeverage.displayOrder.asc())
                .fetch();
    }

    public int findMaxOrderByUser(User user) {
        Integer maxOrder = queryFactory
                .select(favoriteBeverage.displayOrder.max())
                .from(favoriteBeverage)
                .where(favoriteBeverage.user.eq(user))
                .fetchOne();

        return maxOrder != null ? maxOrder : 0;
    }
}
