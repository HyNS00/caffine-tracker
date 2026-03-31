package com.hyuns.cafit.presentation.favorite;

import com.hyuns.cafit.application.favorite.FavoriteBeverageFacade;
import com.hyuns.cafit.application.favorite.dto.FavoriteBeverageResponse;
import com.hyuns.cafit.application.favorite.dto.FavoriteCreateRequest;
import com.hyuns.cafit.application.favorite.dto.FavoriteOrderUpdateRequest;
import com.hyuns.cafit.domain.beverage.BeverageType;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FavoriteBeverageController.class)
class FavoriteBeverageControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private FavoriteBeverageFacade favoriteBeverageFacade;

    @Test
    void 즐겨찾기_추가_성공시_200을_반환한다() throws Exception {
        // given
        FavoriteCreateRequest request = new FavoriteCreateRequest(BeverageType.PRESET, 1L);
        FavoriteBeverageResponse response = new FavoriteBeverageResponse(
            1L, BeverageType.PRESET, 1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0, 1
        );
        given(favoriteBeverageFacade.addFavorite(any(User.class), any(FavoriteCreateRequest.class)))
            .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/favorites")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("아메리카노"))
            .andExpect(jsonPath("$.type").value("PRESET"));
    }

    @Test
    void 즐겨찾기_추가시_타입이_없으면_400을_반환한다() throws Exception {
        // given
        FavoriteCreateRequest request = new FavoriteCreateRequest(null, 1L);

        // when & then
        mockMvc.perform(post("/api/favorites")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 즐겨찾기_추가시_음료ID가_없으면_400을_반환한다() throws Exception {
        // given
        FavoriteCreateRequest request = new FavoriteCreateRequest(BeverageType.PRESET, null);

        // when & then
        mockMvc.perform(post("/api/favorites")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 즐겨찾기_목록을_조회한다() throws Exception {
        // given
        FavoriteBeverageResponse response = new FavoriteBeverageResponse(
            1L, BeverageType.PRESET, 1L, "아메리카노", "스타벅스", "아메리카노", 355, 150.0, 1
        );
        given(favoriteBeverageFacade.getFavorites(any(User.class))).willReturn(List.of(response));

        // when & then
        mockMvc.perform(get("/api/favorites")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].name").value("아메리카노"));
    }

    @Test
    void 즐겨찾기_삭제_성공시_204를_반환한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/api/favorites/1")
                .session(loginSession()))
            .andExpect(status().isNoContent());
    }

    @Test
    void 즐겨찾기_순서_변경_성공시_200을_반환한다() throws Exception {
        // given
        FavoriteOrderUpdateRequest request = new FavoriteOrderUpdateRequest(List.of(3L, 1L, 2L));

        // when & then
        mockMvc.perform(put("/api/favorites/order")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void 즐겨찾기_순서_변경시_목록이_비어있으면_400을_반환한다() throws Exception {
        // given
        FavoriteOrderUpdateRequest request = new FavoriteOrderUpdateRequest(List.of());

        // when & then
        mockMvc.perform(put("/api/favorites/order")
                .session(loginSession())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 세션이_없으면_401을_반환한다() throws Exception {
        // given
        FavoriteCreateRequest request = new FavoriteCreateRequest(BeverageType.PRESET, 1L);

        // when & then
        mockMvc.perform(post("/api/favorites")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized());
    }
}
