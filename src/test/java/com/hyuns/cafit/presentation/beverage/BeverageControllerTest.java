package com.hyuns.cafit.presentation.beverage;

import com.hyuns.cafit.application.beverage.PresetBeverageService;
import com.hyuns.cafit.application.beverage.dto.BeverageCategoryResponse;
import com.hyuns.cafit.application.beverage.dto.PresetBeverageResponse;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BeverageController.class)
class BeverageControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private PresetBeverageService presetBeverageService;

    @Test
    void 전체_음료_목록을_조회한다() throws Exception {
        // given
        PresetBeverageResponse beverage = new PresetBeverageResponse(
            1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0, "스타벅스 아메리카노"
        );
        given(presetBeverageService.getAllBeverages()).willReturn(List.of(beverage));

        // when & then
        mockMvc.perform(get("/api/beverages")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("아메리카노"))
            .andExpect(jsonPath("$[0].brandName").value("스타벅스"));
    }

    @Test
    void 키워드로_음료를_검색한다() throws Exception {
        // given
        PresetBeverageResponse beverage = new PresetBeverageResponse(
            1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0, "스타벅스 아메리카노"
        );
        given(presetBeverageService.searchBeverages("아메리카노")).willReturn(List.of(beverage));

        // when & then
        mockMvc.perform(get("/api/beverages")
                .session(loginSession())
                .param("keyword", "아메리카노"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("아메리카노"));
    }

    @Test
    void 카테고리_목록을_조회한다() throws Exception {
        // given
        BeverageCategoryResponse category = new BeverageCategoryResponse(
            "AMERICANO", "아메리카노", 34.0, 355, 121
        );
        given(presetBeverageService.getAllCategories()).willReturn(List.of(category));

        // when & then
        mockMvc.perform(get("/api/beverages/categories")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].code").value("AMERICANO"))
            .andExpect(jsonPath("$[0].displayName").value("아메리카노"));
    }
}
