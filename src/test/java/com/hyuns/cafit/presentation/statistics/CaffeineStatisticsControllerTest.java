package com.hyuns.cafit.presentation.statistics;

import com.hyuns.cafit.application.statistics.CaffeineStatisticsService;
import com.hyuns.cafit.application.statistics.dto.*;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CaffeineStatisticsController.class)
class CaffeineStatisticsControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private CaffeineStatisticsService caffeineStatisticsService;

    @Test
    void 타임라인을_조회한다() throws Exception {
        // given
        TimelineDataPoint dataPoint = new TimelineDataPoint(LocalDateTime.of(2026, 3, 31, 9, 0), 150.0);
        CaffeineTimelineResponse response = new CaffeineTimelineResponse(
            List.of(dataPoint),
            LocalDateTime.of(2026, 3, 31, 15, 0),
            LocalDateTime.of(2026, 3, 31, 23, 0),
            50.0
        );
        given(caffeineStatisticsService.getTimeline(any(User.class), eq(12))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/statistics/timeline")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.dataPoints[0].caffeineMg").value(150.0))
            .andExpect(jsonPath("$.targetSleepCaffeine").value(50.0));
    }

    @Test
    void 타임라인_조회시_시간을_지정할_수_있다() throws Exception {
        // given
        CaffeineTimelineResponse response = new CaffeineTimelineResponse(
            List.of(),
            LocalDateTime.of(2026, 3, 31, 15, 0),
            LocalDateTime.of(2026, 3, 31, 23, 0),
            50.0
        );
        given(caffeineStatisticsService.getTimeline(any(User.class), eq(24))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/statistics/timeline")
                .session(loginSession())
                .param("hours", "24"))
            .andExpect(status().isOk());
    }

    @Test
    void 일별_통계를_조회한다() throws Exception {
        // given
        StatisticsPeriod period = new StatisticsPeriod(LocalDate.of(2026, 3, 25), LocalDate.of(2026, 3, 31));
        DailyStat dailyStat = new DailyStat(LocalDate.of(2026, 3, 31), 300.0, 3);
        DailyStatisticsResponse response = new DailyStatisticsResponse(
            period, List.of(dailyStat), 250.0, 400.0
        );
        given(caffeineStatisticsService.getDailyStatistics(any(User.class), eq(7))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/statistics/daily")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.periodAverage").value(250.0))
            .andExpect(jsonPath("$.dailyLimit").value(400.0))
            .andExpect(jsonPath("$.dailyStats[0].totalCaffeineMg").value(300.0));
    }

    @Test
    void 인기_음료를_조회한다() throws Exception {
        // given
        TopBeverageStat stat = new TopBeverageStat("아메리카노", "스타벅스", 355, 5);
        given(caffeineStatisticsService.getTopBeverages(any(User.class), eq(7)))
            .willReturn(List.of(stat));

        // when & then
        mockMvc.perform(get("/api/statistics/top-beverages")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].beverageName").value("아메리카노"))
            .andExpect(jsonPath("$[0].count").value(5));
    }

    @Test
    void 세션이_없으면_401을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/statistics/timeline"))
            .andExpect(status().isUnauthorized());
    }
}
