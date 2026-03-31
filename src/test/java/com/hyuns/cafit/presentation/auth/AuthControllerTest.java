package com.hyuns.cafit.presentation.auth;

import com.hyuns.cafit.application.auth.AuthService;
import com.hyuns.cafit.application.auth.dto.AuthResponse;
import com.hyuns.cafit.application.auth.dto.LoginRequest;
import com.hyuns.cafit.application.auth.dto.SignUpRequest;
import com.hyuns.cafit.presentation.CommonControllerSliceTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
class AuthControllerTest extends CommonControllerSliceTestSupport {

    @MockBean
    private AuthService authService;

    @Test
    void 회원가입_성공시_200을_반환한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("user@example.com", "password1234", "홍길동");
        AuthResponse response = new AuthResponse(1L, "user@example.com", "홍길동");
        given(authService.signup(any(SignUpRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.email").value("user@example.com"))
            .andExpect(jsonPath("$.name").value("홍길동"));
    }

    @Test
    void 회원가입시_이메일_형식이_잘못되면_400을_반환한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("invalid-email", "password1234", "홍길동");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 회원가입시_비밀번호가_4자_미만이면_400을_반환한다() throws Exception {
        // given
        SignUpRequest request = new SignUpRequest("user@example.com", "ab", "홍길동");

        // when & then
        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인_성공시_200을_반환한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("user@example.com", "password1234");
        AuthResponse response = new AuthResponse(1L, "user@example.com", "홍길동");
        given(authService.login(any(LoginRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(1L))
            .andExpect(jsonPath("$.email").value("user@example.com"));
    }

    @Test
    void 로그인시_이메일이_비어있으면_400을_반환한다() throws Exception {
        // given
        LoginRequest request = new LoginRequest("", "password1234");

        // when & then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void 로그아웃_성공시_200을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/logout")
                .session(loginSession()))
            .andExpect(status().isOk());
    }

    @Test
    void 현재_사용자_조회시_200을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/me")
                .session(loginSession()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(TEST_USER_ID))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.name").value("테스트유저"));
    }

    @Test
    void 현재_사용자_조회시_세션이_없으면_401을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized());
    }
}
