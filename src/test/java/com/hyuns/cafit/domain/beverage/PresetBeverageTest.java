package com.hyuns.cafit.domain.beverage;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class PresetBeverageTest {

    private PresetBeverage createBeverage() {
        return new PresetBeverage("아메리카노", "스타벅스", BeverageCategory.AMERICANO, 355, 150.0);
    }

    @Test
    void getDisplayName은_브랜드_이름_용량_포맷으로_반환한다() {
        PresetBeverage beverage = createBeverage();

        assertThat(beverage.getDisplayName()).isEqualTo("스타벅스 아메리카노 (355ml)");
    }

    @Test
    void getCaffeineMgPer100ml은_100ml당_카페인을_계산한다() {
        PresetBeverage beverage = createBeverage();

        double expected = (150.0 / 355) * 100.0;
        assertThat(beverage.getCaffeineMgPer100ml()).isCloseTo(expected, Offset.offset(0.01));
    }

    @Test
    void estimateCaffeineForVolume은_다른_용량에_대한_카페인을_추정한다() {
        PresetBeverage beverage = createBeverage();

        double estimated = beverage.estimateCaffeineForVolume(500);
        double expected = beverage.getCaffeineMgPer100ml() * 500 / 100.0;
        assertThat(estimated).isCloseTo(expected, Offset.offset(0.01));
    }

    @Test
    void estimateCaffeineForVolume은_같은_용량이면_원래_카페인과_동일하다() {
        PresetBeverage beverage = createBeverage();

        assertThat(beverage.estimateCaffeineForVolume(355)).isCloseTo(150.0, Offset.offset(0.01));
    }
}
