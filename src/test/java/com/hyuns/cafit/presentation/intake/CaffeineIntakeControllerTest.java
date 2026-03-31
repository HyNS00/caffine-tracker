package com.hyuns.cafit.presentation.intake;

import com.hyuns.cafit.application.intake.CaffeineIntakeService;
import com.hyuns.cafit.application.intake.dto.CaffeineIntakeCreateRequest;
import com.hyuns.cafit.application.intake.dto.CaffeineIntakeResponse;
import com.hyuns.cafit.domain.beverage.BeverageType;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CaffeineIntakeController.class)
class CaffeineIntakeControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private CaffeineIntakeService caffeineIntakeService;

    @Test
    void 프리셋_음료_섭취_기록_성공시_200을_반환한다() throws Exception {
        // given
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 31, 9, 0);
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(consumedAt);
        CaffeineIntakeResponse response = new CaffeineIntakeResponse(
            1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0,
            consumedAt, "스타벅스 아메리카노", BeverageType.PRESET, 1L
        );
        given(caffeineIntakeService.recordPresetIntake(any(User.class), eq(1L), any(CaffeineIntakeCreateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/intakes/preset/1")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.beverageName").value("아메리카노"))
            .andExpect(jsonPath("$.sourceType").value("PRESET"));
    }

    @Test
    void 커스텀_음료_섭취_기록_성공시_200을_반환한다() throws Exception {
        // given
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 31, 14, 0);
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(consumedAt);
        CaffeineIntakeResponse response = new CaffeineIntakeResponse(
            2L, "내 커피", null, "아메리카노", 300, 120.0,
            consumedAt, "내 커피", BeverageType.CUSTOM, 1L
        );
        given(caffeineIntakeService.recordCustomIntake(any(User.class), eq(1L), any(CaffeineIntakeCreateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/intakes/custom/1")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2L))
            .andExpect(jsonPath("$.sourceType").value("CUSTOM"));
    }

    @Test
    void 섭취_기록시_섭취시간이_없으면_400을_반환한다() throws Exception {
        // given
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(null);

        // when & then
        mockMvc.perform(post("/api/intakes/preset/1")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 오늘_섭취_기록을_조회한다() throws Exception {
        // given
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 31, 9, 0);
        CaffeineIntakeResponse response = new CaffeineIntakeResponse(
            1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0,
            consumedAt, "스타벅스 아메리카노", BeverageType.PRESET, 1L
        );
        given(caffeineIntakeService.getTodayIntakes(any(User.class))).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/intakes/today")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].beverageName").value("아메리카노"));
    }

    @Test
    void 섭취_기록_삭제_성공시_204를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/intakes/1")
                .session(loginSession()))
            .andExpect(status().isNoContent());
    }

    @Test
    void 세션이_없으면_401을_반환한다() throws Exception {
        // given
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(LocalDateTime.now());

        // when & then
        mockMvc.perform(post("/api/intakes/preset/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
