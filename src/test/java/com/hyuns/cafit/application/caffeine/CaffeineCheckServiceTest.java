package com.hyuns.cafit.application.caffeine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.application.beverage.dto.BeverageInfo;
import com.hyuns.cafit.application.caffeine.dto.CaffeineStatus;
import com.hyuns.cafit.application.caffeine.dto.CurrentCaffeineResponse;
import com.hyuns.cafit.application.caffeine.dto.DrinkCheckResponse;
import com.hyuns.cafit.application.caffeine.dto.DrinkRecommendation;
import com.hyuns.cafit.context.IntegrationTest;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CaffeineCheckServiceTest {

    @Autowired
    private CaffeineCheckService caffeineCheckService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 섭취_기록이_없으면_현재_카페인이_0이다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        CurrentCaffeineResponse response = caffeineCheckService.getCurrentStatus(user);

        // then
        CaffeineStatus status = response.status();
        assertAll(
                () -> assertThat(status.currentMg()).isEqualTo(0.0),
                () -> assertThat(status.todayTotalMg()).isEqualTo(0.0),
                () -> assertThat(status.predictedAtBedtimeMg()).isEqualTo(0.0),
                () -> assertThat(status.hoursUntilBedtime()).isGreaterThan(0),
                () -> assertThat(response.recommendation()).isEqualTo(DrinkRecommendation.SAFE)
        );
    }

    @Test
    @Sql("/sql/intake/insert_caffeine_intakes.sql")
    void 섭취_기록이_있으면_현재_카페인이_0보다_크다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        CurrentCaffeineResponse response = caffeineCheckService.getCurrentStatus(user);

        // then
        assertThat(response.status().currentMg()).isGreaterThan(0);
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 음료_체크_시_전후_상태를_비교할_수_있다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        BeverageInfo beverageInfo = new BeverageInfo("아메리카노", 150.0);

        // when
        DrinkCheckResponse response = caffeineCheckService.checkBeverage(user, beverageInfo);

        // then
        assertAll(
                () -> assertThat(response.beverage().name()).isEqualTo("아메리카노"),
                () -> assertThat(response.beverage().caffeineMg()).isEqualTo(150.0),
                () -> assertThat(response.after().currentMg())
                        .isGreaterThan(response.before().currentMg()),
                () -> assertThat(response.after().todayTotalMg())
                        .isEqualTo(response.before().todayTotalMg() + 150.0)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 섭취_기록이_없는_상태에서_음료_체크_시_안전_추천이다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        BeverageInfo beverageInfo = new BeverageInfo("녹차", 30.0);

        // when
        DrinkCheckResponse response = caffeineCheckService.checkBeverage(user, beverageInfo);

        // then
        assertAll(
                () -> assertThat(response.isSafe()).isTrue(),
                () -> assertThat(response.recommendation()).isEqualTo(DrinkRecommendation.SAFE)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 일일_한도_초과_음료_체크_시_위험_추천이다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        BeverageInfo beverageInfo = new BeverageInfo("초고카페인", 500.0);

        // when
        DrinkCheckResponse response = caffeineCheckService.checkBeverage(user, beverageInfo);

        // then
        assertThat(response.recommendation()).isEqualTo(DrinkRecommendation.DANGER);
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 현재_상태_조회_시_사용자_설정이_포함된다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        CurrentCaffeineResponse response = caffeineCheckService.getCurrentStatus(user);

        // then
        assertAll(
                () -> assertThat(response.settings().dailyLimitMg()).isEqualTo(400),
                () -> assertThat(response.settings().halfLifeHours()).isEqualTo(5.0),
                () -> assertThat(response.settings().targetSleepCaffeineMg()).isEqualTo(50.0)
        );
    }
}
