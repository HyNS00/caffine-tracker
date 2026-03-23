package com.hyuns.cafit.application.intake;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.application.intake.dto.CaffeineIntakeCreateRequest;
import com.hyuns.cafit.application.intake.dto.CaffeineIntakeResponse;
import com.hyuns.cafit.context.IntegrationTest;
import com.hyuns.cafit.domain.beverage.BeverageType;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import com.hyuns.cafit.global.exception.BeverageAccessDeniedException;
import com.hyuns.cafit.global.exception.BeverageNotFoundException;
import com.hyuns.cafit.global.exception.CustomBeverageNotFoundException;
import com.hyuns.cafit.global.exception.IntakeAccessDeniedException;
import com.hyuns.cafit.global.exception.IntakeNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

@IntegrationTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CaffeineIntakeServiceTest {

    @Autowired
    private CaffeineIntakeService caffeineIntakeService;

    @Autowired
    private UserRepository userRepository;

    @Test
    @Sql({"/sql/auth/insert_user.sql", "/sql/beverage/insert_preset_beverages.sql"})
    void 프리셋_음료_섭취를_기록한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 23, 9, 0);
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(consumedAt);

        // when
        CaffeineIntakeResponse response = caffeineIntakeService.recordPresetIntake(user, 1L, request);

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.beverageName()).isEqualTo("아메리카노"),
                () -> assertThat(response.brandName()).isEqualTo("스타벅스"),
                () -> assertThat(response.caffeineMg()).isEqualTo(150.0),
                () -> assertThat(response.volumeMl()).isEqualTo(355),
                () -> assertThat(response.consumedAt()).isEqualTo(consumedAt),
                () -> assertThat(response.sourceType()).isEqualTo(BeverageType.PRESET),
                () -> assertThat(response.sourceBeverageId()).isEqualTo(1L)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 존재하지_않는_프리셋_음료를_기록하면_예외가_발생한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> caffeineIntakeService.recordPresetIntake(user, 999L, request))
                .isInstanceOf(BeverageNotFoundException.class);
    }

    @Test
    @Sql("/sql/beverage/insert_custom_beverages.sql")
    void 커스텀_음료_섭취를_기록한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        LocalDateTime consumedAt = LocalDateTime.of(2026, 3, 23, 10, 0);
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(consumedAt);

        // when
        CaffeineIntakeResponse response = caffeineIntakeService.recordCustomIntake(user, 1L, request);

        // then
        assertAll(
                () -> assertThat(response.id()).isNotNull(),
                () -> assertThat(response.beverageName()).isEqualTo("내 커피"),
                () -> assertThat(response.caffeineMg()).isEqualTo(120.0),
                () -> assertThat(response.sourceType()).isEqualTo(BeverageType.CUSTOM),
                () -> assertThat(response.sourceBeverageId()).isEqualTo(1L)
        );
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 존재하지_않는_커스텀_음료를_기록하면_예외가_발생한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> caffeineIntakeService.recordCustomIntake(user, 999L, request))
                .isInstanceOf(CustomBeverageNotFoundException.class);
    }

    @Test
    @Sql({"/sql/beverage/insert_custom_beverages.sql", "/sql/auth/insert_other_user.sql"})
    void 다른_사용자의_커스텀_음료를_기록하면_예외가_발생한다() {
        // given
        User otherUser = userRepository.findById(2L).orElseThrow();
        CaffeineIntakeCreateRequest request = new CaffeineIntakeCreateRequest(LocalDateTime.now());

        // when & then
        assertThatThrownBy(() -> caffeineIntakeService.recordCustomIntake(otherUser, 1L, request))
                .isInstanceOf(BeverageAccessDeniedException.class);
    }

    @Test
    @Sql("/sql/intake/insert_caffeine_intakes.sql")
    void 오늘_섭취_기록을_조회한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        List<CaffeineIntakeResponse> responses = caffeineIntakeService.getTodayIntakes(user);

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses).allSatisfy(response ->
                assertThat(response.beverageName()).isEqualTo("아메리카노")
        );
    }

    @Test
    @Sql("/sql/intake/insert_caffeine_intakes.sql")
    void 섭취_기록을_삭제한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when
        caffeineIntakeService.deleteIntake(1L, user);

        // then
        assertThatThrownBy(() -> caffeineIntakeService.deleteIntake(1L, user))
                .isInstanceOf(IntakeNotFoundException.class);
    }

    @Test
    @Sql("/sql/auth/insert_user.sql")
    void 존재하지_않는_섭취_기록을_삭제하면_예외가_발생한다() {
        // given
        User user = userRepository.findById(1L).orElseThrow();

        // when & then
        assertThatThrownBy(() -> caffeineIntakeService.deleteIntake(999L, user))
                .isInstanceOf(IntakeNotFoundException.class);
    }

    @Test
    @Sql({"/sql/intake/insert_caffeine_intakes.sql", "/sql/auth/insert_other_user.sql"})
    void 다른_사용자의_섭취_기록을_삭제하면_예외가_발생한다() {
        // given
        User otherUser = userRepository.findById(2L).orElseThrow();

        // when & then
        assertThatThrownBy(() -> caffeineIntakeService.deleteIntake(1L, otherUser))
                .isInstanceOf(IntakeAccessDeniedException.class);
    }
}
