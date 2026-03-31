package com.hyuns.cafit.presentation.caffeine;

import com.hyuns.cafit.application.caffeine.CaffeineCheckFacade;
import com.hyuns.cafit.application.beverage.dto.BeverageInfo;
import com.hyuns.cafit.application.caffeine.dto.*;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CaffeineCheckController.class)
class CaffeineCheckControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private CaffeineCheckFacade caffeineCheckFacade;

    @Test
    void 현재_카페인_상태를_조회한다() throws Exception {
        // given
        CaffeineStatus caffeineStatus = new CaffeineStatus(100.0, 30.0, 200.0, 8.0);
        UserCaffeineSettings settings = new UserCaffeineSettings(400.0, 50.0, 5.0, LocalTime.of(23, 0));
        CurrentCaffeineResponse response = new CurrentCaffeineResponse(
            caffeineStatus, settings, DrinkRecommendation.SAFE
        );
        given(caffeineCheckFacade.getCurrentStatus(any(User.class))).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/caffeine/status")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status.currentMg").value(100.0))
            .andExpect(jsonPath("$.recommendation").value("SAFE"));
    }

    @Test
    void 프리셋_음료_체크_성공시_200을_반환한다() throws Exception {
        // given
        CaffeineStatus before = new CaffeineStatus(100.0, 30.0, 200.0, 8.0);
        CaffeineStatus after = new CaffeineStatus(250.0, 75.0, 350.0, 8.0);
        UserCaffeineSettings settings = new UserCaffeineSettings(400.0, 50.0, 5.0, LocalTime.of(23, 0));
        BeverageInfo beverageInfo = new BeverageInfo("스타벅스 아메리카노", 150.0);
        DrinkCheckResponse response = new DrinkCheckResponse(
            beverageInfo, before, after, settings, DrinkRecommendation.WARNING, true
        );
        given(caffeineCheckFacade.checkPresetBeverage(any(User.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/caffeine/check/preset/1")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.beverage.name").value("스타벅스 아메리카노"))
            .andExpect(jsonPath("$.isSafe").value(true));
    }

    @Test
    void 커스텀_음료_체크_성공시_200을_반환한다() throws Exception {
        // given
        CaffeineStatus before = new CaffeineStatus(100.0, 30.0, 200.0, 8.0);
        CaffeineStatus after = new CaffeineStatus(250.0, 75.0, 350.0, 8.0);
        UserCaffeineSettings settings = new UserCaffeineSettings(400.0, 50.0, 5.0, LocalTime.of(23, 0));
        BeverageInfo beverageInfo = new BeverageInfo("내 커피", 150.0);
        DrinkCheckResponse response = new DrinkCheckResponse(
            beverageInfo, before, after, settings, DrinkRecommendation.SAFE, true
        );
        given(caffeineCheckFacade.checkCustomBeverage(any(User.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/caffeine/check/custom/1")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.beverage.name").value("내 커피"));
    }

    @Test
    void 세션이_없으면_401을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/caffeine/status"))
            .andExpect(status().isUnauthorized());
    }
}
