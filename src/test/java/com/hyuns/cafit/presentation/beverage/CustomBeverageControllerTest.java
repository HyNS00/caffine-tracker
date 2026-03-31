package com.hyuns.cafit.presentation.beverage;

import com.hyuns.cafit.application.beverage.CustomBeverageService;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageCreateRequest;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageResponse;
import com.hyuns.cafit.application.beverage.dto.CustomBeverageUpdateRequest;
import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CustomBeverageController.class)
class CustomBeverageControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private CustomBeverageService customBeverageService;

    @Test
    void 커스텀_음료_생성_성공시_200을_반환한다() throws Exception {
        // given
        CustomBeverageCreateRequest request = new CustomBeverageCreateRequest(
            "내 커피", BeverageCategory.AMERICANO, 300, 150.0
        );
        CustomBeverageResponse response = new CustomBeverageResponse(
            1L, "내 커피", "아메리카노", 300, 150.0, "내 커피"
        );
        given(customBeverageService.createCustomBeverage(any(User.class), any(CustomBeverageCreateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/beverages/custom")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("내 커피"));
    }

    @Test
    void 커스텀_음료_생성시_이름이_비어있으면_400을_반환한다() throws Exception {
        // given
        CustomBeverageCreateRequest request = new CustomBeverageCreateRequest(
            "", BeverageCategory.AMERICANO, 300, 150.0
        );

        // when & then
        mockMvc.perform(post("/api/beverages/custom")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 커스텀_음료_생성시_용량이_0이면_400을_반환한다() throws Exception {
        // given
        CustomBeverageCreateRequest request = new CustomBeverageCreateRequest(
            "내 커피", BeverageCategory.AMERICANO, 0, 150.0
        );

        // when & then
        mockMvc.perform(post("/api/beverages/custom")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 내_커스텀_음료_목록을_조회한다() throws Exception {
        // given
        CustomBeverageResponse response = new CustomBeverageResponse(
            1L, "내 커피", "아메리카노", 300, 150.0, "내 커피"
        );
        given(customBeverageService.getMyCustomBeverages(any(User.class)))
            .willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/beverages/custom")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("내 커피"));
    }

    @Test
    void 커스텀_음료_수정_성공시_200을_반환한다() throws Exception {
        // given
        CustomBeverageUpdateRequest request = new CustomBeverageUpdateRequest("수정 커피", 400, 200.0);
        CustomBeverageResponse response = new CustomBeverageResponse(
            1L, "수정 커피", "아메리카노", 400, 200.0, "수정 커피"
        );
        given(customBeverageService.updateCustomBeverage(eq(1L), any(User.class), any(CustomBeverageUpdateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(put("/api/beverages/custom/1")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("수정 커피"));
    }

    @Test
    void 커스텀_음료_삭제_성공시_204를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/beverages/custom/1")
                .session(loginSession()))
            .andExpect(status().isNoContent());
    }

    @Test
    void 세션이_없으면_401을_반환한다() throws Exception {
        // given
        CustomBeverageCreateRequest request = new CustomBeverageCreateRequest(
            "내 커피", BeverageCategory.AMERICANO, 300, 150.0
        );

        // when & then
        mockMvc.perform(post("/api/beverages/custom")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
