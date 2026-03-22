package com.hyuns.cafit.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalTime;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class UserTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void 생성_시_기본값이_올바르게_설정된다() {
        User user = new User("test@email.com", "password", "테스트");

        assertAll(
                () -> assertThat(user.getEmail()).isEqualTo("test@email.com"),
                () -> assertThat(user.getPassword()).isEqualTo("password"),
                () -> assertThat(user.getName()).isEqualTo("테스트"),
                () -> assertThat(user.getDailyCaffeineLimit()).isEqualTo(400),
                () -> assertThat(user.getCaffeineHalfLife()).isEqualTo(5.0),
                () -> assertThat(user.getBedTime()).isEqualTo(LocalTime.of(23, 0)),
                () -> assertThat(user.getTargetSleepCaffeine()).isEqualTo(50.0)
        );
    }

    @Test
    void 올바른_비밀번호면_true를_반환한다() {
        String rawPassword = "password123";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User("test@email.com", encodedPassword, "테스트");

        assertThat(user.isPasswordMatch(rawPassword, passwordEncoder)).isTrue();
    }

    @Test
    void 틀린_비밀번호면_false를_반환한다() {
        String encodedPassword = passwordEncoder.encode("password123");
        User user = new User("test@email.com", encodedPassword, "테스트");

        assertThat(user.isPasswordMatch("wrongPassword", passwordEncoder)).isFalse();
    }
}
