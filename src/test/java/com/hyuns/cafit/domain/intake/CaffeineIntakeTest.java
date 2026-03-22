package com.hyuns.cafit.domain.intake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CaffeineIntakeTest {

    private User createUser(Long id) {
        User user = new User("test@email.com", "password", "테스트");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private PresetBeverage createPresetBeverage() {
        PresetBeverage beverage = new PresetBeverage(
                "아메리카노", "스타벅스", BeverageCategory.AMERICANO, 355, 150.0);
        ReflectionTestUtils.setField(beverage, "id", 1L);
        return beverage;
    }

    private CustomBeverage createCustomBeverage(User user) {
        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.LATTE, 400, 180.0);
        ReflectionTestUtils.setField(beverage, "id", 1L);
        return beverage;
    }

    @Test
    void fromPreset으로_프리셋_음료_섭취_기록을_생성한다() {
        User user = createUser(1L);
        PresetBeverage beverage = createPresetBeverage();
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 22, 10, 0);

        CaffeineIntake intake = CaffeineIntake.fromPreset(user, beverage, consumedAt);

        assertAll(
                () -> assertThat(intake.getUser()).isEqualTo(user),
                () -> assertThat(intake.getBeverageName()).isEqualTo("아메리카노"),
                () -> assertThat(intake.getBrandName()).isEqualTo("스타벅스"),
                () -> assertThat(intake.getCategory()).isEqualTo(BeverageCategory.AMERICANO),
                () -> assertThat(intake.getVolumeMl()).isEqualTo(355),
                () -> assertThat(intake.getCaffeineMg()).isEqualTo(150.0),
                () -> assertThat(intake.getConsumedAt()).isEqualTo(consumedAt),
                () -> assertThat(intake.getSourceType().isPreset()).isTrue(),
                () -> assertThat(intake.getSourceBeverageId()).isEqualTo(1L)
        );
    }

    @Test
    void fromCustom으로_커스텀_음료_섭취_기록을_생성한다() {
        User user = createUser(1L);
        CustomBeverage beverage = createCustomBeverage(user);
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 22, 14, 30);

        CaffeineIntake intake = CaffeineIntake.fromCustom(user, beverage, consumedAt);

        assertAll(
                () -> assertThat(intake.getUser()).isEqualTo(user),
                () -> assertThat(intake.getBeverageName()).isEqualTo("나만의 커피"),
                () -> assertThat(intake.getBrandName()).isNull(),
                () -> assertThat(intake.getCategory()).isEqualTo(BeverageCategory.LATTE),
                () -> assertThat(intake.getVolumeMl()).isEqualTo(400),
                () -> assertThat(intake.getCaffeineMg()).isEqualTo(180.0),
                () -> assertThat(intake.getConsumedAt()).isEqualTo(consumedAt),
                () -> assertThat(intake.getSourceType().isPreset()).isFalse(),
                () -> assertThat(intake.getSourceBeverageId()).isEqualTo(1L)
        );
    }

    @Test
    void getDisplayName은_프리셋_음료일_때_브랜드명을_포함한다() {
        User user = createUser(1L);
        PresetBeverage beverage = createPresetBeverage();
        CaffeineIntake intake = CaffeineIntake.fromPreset(user, beverage,
                LocalDateTime.of(2026, 3, 22, 10, 0));

        assertThat(intake.getDisplayName()).isEqualTo("아메리카노(스타벅스) 355ml - 150mg");
    }

    @Test
    void getDisplayName은_커스텀_음료일_때_브랜드명이_없다() {
        User user = createUser(1L);
        CustomBeverage beverage = createCustomBeverage(user);
        CaffeineIntake intake = CaffeineIntake.fromCustom(user, beverage,
                LocalDateTime.of(2026, 3, 22, 14, 30));

        assertThat(intake.getDisplayName()).isEqualTo("나만의 커피 400ml - 180mg");
    }

    @Test
    void isOwnedBy는_소유자인_경우_true를_반환한다() {
        User user = createUser(1L);
        PresetBeverage beverage = createPresetBeverage();
        CaffeineIntake intake = CaffeineIntake.fromPreset(user, beverage, LocalDateTime.now());

        assertThat(intake.isOwnedBy(user)).isTrue();
    }

    @Test
    void isOwnedBy는_소유자가_아닌_경우_false를_반환한다() {
        User owner = createUser(1L);
        User other = createUser(2L);
        PresetBeverage beverage = createPresetBeverage();
        CaffeineIntake intake = CaffeineIntake.fromPreset(owner, beverage, LocalDateTime.now());

        assertThat(intake.isOwnedBy(other)).isFalse();
    }
}
