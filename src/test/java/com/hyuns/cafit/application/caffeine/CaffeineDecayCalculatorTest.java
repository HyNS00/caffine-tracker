package com.hyuns.cafit.application.caffeine;

import static org.assertj.core.api.Assertions.assertThat;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.intake.CaffeineIntake;
import com.hyuns.cafit.domain.user.User;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.data.Offset;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CaffeineDecayCalculatorTest {

    private final CaffeineDecayCalculator calculator = new CaffeineDecayCalculator();
    private static final double HALF_LIFE = 5.0;

    @Test
    void 경과_시간이_0이면_초기량을_그대로_반환한다() {
        assertThat(calculator.calculateRemaining(100.0, 0, HALF_LIFE)).isEqualTo(100.0);
    }

    @Test
    void 경과_시간이_음수면_초기량을_그대로_반환한다() {
        assertThat(calculator.calculateRemaining(100.0, -1.0, HALF_LIFE)).isEqualTo(100.0);
    }

    @Test
    void 반감기만큼_지나면_절반으로_줄어든다() {
        double remaining = calculator.calculateRemaining(100.0, HALF_LIFE, HALF_LIFE);

        assertThat(remaining).isCloseTo(50.0, Offset.offset(0.01));
    }

    @Test
    void 반감기_두_배만큼_지나면_4분의_1로_줄어든다() {
        double remaining = calculator.calculateRemaining(100.0, HALF_LIFE * 2, HALF_LIFE);

        assertThat(remaining).isCloseTo(25.0, Offset.offset(0.01));
    }

    @Test
    void 반감기_세_배만큼_지나면_8분의_1로_줄어든다() {
        double remaining = calculator.calculateRemaining(100.0, HALF_LIFE * 3, HALF_LIFE);

        assertThat(remaining).isCloseTo(12.5, Offset.offset(0.01));
    }

    @Test
    void 반감기가_짧으면_더_빠르게_감소한다() {
        double shortHalfLife = calculator.calculateRemaining(100.0, 5.0, 3.0);
        double longHalfLife = calculator.calculateRemaining(100.0, 5.0, 7.0);

        assertThat(shortHalfLife).isLessThan(longHalfLife);
    }

    @Test
    void caffeineLevelAt은_여러_섭취_기록의_잔존량을_합산한다() {
        User user = new User("test@email.com", "password", "테스트");
        ReflectionTestUtils.setField(user, "id", 1L);
        PresetBeverage beverage = new PresetBeverage(
                "아메리카노", "스타벅스", BeverageCategory.AMERICANO, 355, 150.0);
        ReflectionTestUtils.setField(beverage, "id", 1L);

        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 15, 0);
        CaffeineIntake intake1 = CaffeineIntake.fromPreset(user, beverage, now.minusHours(5));
        CaffeineIntake intake2 = CaffeineIntake.fromPreset(user, beverage, now.minusHours(1));

        double total = calculator.caffeineLevelAt(List.of(intake1, intake2), now, HALF_LIFE);

        double expected1 = calculator.calculateRemaining(150.0, 5.0, HALF_LIFE);
        double expected2 = calculator.calculateRemaining(150.0, 1.0, HALF_LIFE);
        assertThat(total).isCloseTo(expected1 + expected2, Offset.offset(0.01));
    }

    @Test
    void caffeineLevelAt은_빈_리스트면_0을_반환한다() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 15, 0);

        double total = calculator.caffeineLevelAt(List.of(), now, HALF_LIFE);

        assertThat(total).isEqualTo(0.0);
    }

    @Test
    void caffeineLevelAt은_미래_섭취는_감소없이_전량_포함한다() {
        User user = new User("test@email.com", "password", "테스트");
        ReflectionTestUtils.setField(user, "id", 1L);
        PresetBeverage beverage = new PresetBeverage(
                "아메리카노", "스타벅스", BeverageCategory.AMERICANO, 355, 150.0);
        ReflectionTestUtils.setField(beverage, "id", 1L);

        LocalDateTime now = LocalDateTime.of(2026, 3, 22, 15, 0);
        CaffeineIntake futureIntake = CaffeineIntake.fromPreset(user, beverage, now.plusHours(1));

        double total = calculator.caffeineLevelAt(List.of(futureIntake), now, HALF_LIFE);

        assertThat(total).isEqualTo(150.0);
    }
}
