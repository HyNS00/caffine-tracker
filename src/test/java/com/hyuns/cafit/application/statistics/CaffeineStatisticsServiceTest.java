package com.hyuns.cafit.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.application.statistics.dto.CaffeineTimelineResponse;
import com.hyuns.cafit.application.statistics.dto.DailyStatisticsResponse;
import com.hyuns.cafit.application.statistics.dto.TopBeverageStat;
import com.hyuns.cafit.context.IntegrationTest;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CaffeineStatisticsServiceTest {

    @Autowired
    private CaffeineStatisticsService caffeineStatisticsService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql("/sql/intake/insert_caffeine_intakes.sql")
    void 타임라인을_조회하면_시간별_데이터포인트가_생성된다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        int hours = 12;

        // when
        CaffeineTimelineResponse response = caffeineStatisticsService.getTimeline(user, hours);

        // then
        assertAll(
                () -> assertThat(response.dataPoints()).hasSize(hours + 1),
                () -> assertThat(response.currentTime()).isNotNull(),
                () -> assertThat(response.bedtime()).isNotNull(),
                () -> assertThat(response.targetSleepCaffeine()).isEqualTo(50.0)
        );
    }

    @Test
    @Sql("/sql/intake/insert_caffeine_intakes.sql")
    void 타임라인의_카페인은_시간이_지날수록_감소한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        CaffeineTimelineResponse response = caffeineStatisticsService.getTimeline(user, 6);

        // then
        double firstPoint = response.dataPoints().get(0).caffeineMg();
        double lastPoint = response.dataPoints().get(6).caffeineMg();
        assertThat(firstPoint).isGreaterThan(lastPoint);
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 섭취_기록이_없으면_타임라인_카페인이_모두_0이다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        CaffeineTimelineResponse response = caffeineStatisticsService.getTimeline(user, 6);

        // then
        assertThat(response.dataPoints()).allSatisfy(dataPoint ->
                assertThat(dataPoint.caffeineMg()).isEqualTo(0.0)
        );
    }

    @Test
    @Sql("/sql/statistics/insert_statistics_data.sql")
    void 일별_통계를_조회한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        int days = 7;

        // when
        DailyStatisticsResponse response = caffeineStatisticsService.getDailyStatistics(user, days);

        // then
        assertAll(
                () -> assertThat(response.dailyStats()).hasSize(days),
                () -> assertThat(response.dailyLimit()).isEqualTo(400),
                () -> assertThat(response.periodAverage()).isGreaterThan(0)
        );
    }

    @Test
    @Sql("/sql/statistics/insert_statistics_data.sql")
    void 오늘_일별_통계에_섭취_기록이_포함된다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        DailyStatisticsResponse response = caffeineStatisticsService.getDailyStatistics(user, 1);

        // then
        assertAll(
                () -> assertThat(response.dailyStats()).hasSize(1),
                () -> assertThat(response.dailyStats().get(0).totalCaffeineMg()).isGreaterThan(0),
                () -> assertThat(response.dailyStats().get(0).intakeCount()).isGreaterThanOrEqualTo(2)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 섭취_기록이_없으면_일별_통계가_모두_0이다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        DailyStatisticsResponse response = caffeineStatisticsService.getDailyStatistics(user, 7);

        // then
        assertThat(response.dailyStats()).allSatisfy(stat -> assertAll(
                () -> assertThat(stat.totalCaffeineMg()).isEqualTo(0.0),
                () -> assertThat(stat.intakeCount()).isEqualTo(0)
        ));
    }

    @Test
    @Sql("/sql/statistics/insert_statistics_data.sql")
    void 인기_음료를_조회한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        List<TopBeverageStat> topBeverages = caffeineStatisticsService.getTopBeverages(user, 7);

        // then
        assertAll(
                () -> assertThat(topBeverages).isNotEmpty(),
                () -> assertThat(topBeverages).hasSizeLessThanOrEqualTo(3),
                () -> assertThat(topBeverages.get(0).beverageName()).isEqualTo("아메리카노"),
                () -> assertThat(topBeverages.get(0).count()).isGreaterThanOrEqualTo(3)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 섭취_기록이_없으면_인기_음료가_비어있다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        List<TopBeverageStat> topBeverages = caffeineStatisticsService.getTopBeverages(user, 7);

        // then
        assertThat(topBeverages).isEmpty();
    }
}
