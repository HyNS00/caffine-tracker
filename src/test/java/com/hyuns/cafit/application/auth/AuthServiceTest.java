package com.hyuns.cafit.application.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.application.auth.dto.AuthResponse;
import com.hyuns.cafit.application.auth.dto.LoginRequest;
import com.hyuns.cafit.application.auth.dto.SignUpRequest;
import com.hyuns.cafit.context.IntegrationTest;
import com.hyuns.cafit.global.exception.DuplicateEmailException;
import com.hyuns.cafit.global.exception.LoginFailedException;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@IntegrationTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void 회원가입에_성공한다() {
        // given
        SignUpRequest request = new SignUpRequest("new@example.com", "password123", "새유저");

        // when
        AuthResponse response = authService.signup(request);

        // then
        assertAll(
                () -> assertThat(response.userId()).isNotNull(),
                () -> assertThat(response.email()).isEqualTo("new@example.com"),
                () -> assertThat(response.name()).isEqualTo("새유저")
        );
    }

    @Test
    void 중복_이메일로_회원가입하면_예외가_발생한다() {
        // given
        SignUpRequest request = new SignUpRequest("dup@example.com", "password123", "유저1");
        authService.signup(request);

        SignUpRequest duplicateRequest = new SignUpRequest("dup@example.com", "password456", "유저2");

        // when & then
        assertThatThrownBy(() -> authService.signup(duplicateRequest))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void 로그인에_성공한다() {
        // given
        authService.signup(new SignUpRequest("login@example.com", "password123", "로그인유저"));
        LoginRequest request = new LoginRequest("login@example.com", "password123");

        // when
        AuthResponse response = authService.login(request);

        // then
        assertAll(
                () -> assertThat(response.userId()).isNotNull(),
                () -> assertThat(response.email()).isEqualTo("login@example.com"),
                () -> assertThat(response.name()).isEqualTo("로그인유저")
        );
    }

    @Test
    void 존재하지_않는_이메일로_로그인하면_예외가_발생한다() {
        // given
        LoginRequest request = new LoginRequest("notfound@example.com", "password123");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }

    @Test
    void 잘못된_비밀번호로_로그인하면_예외가_발생한다() {
        // given
        authService.signup(new SignUpRequest("wrong@example.com", "password123", "유저"));
        LoginRequest request = new LoginRequest("wrong@example.com", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(LoginFailedException.class);
    }
}
