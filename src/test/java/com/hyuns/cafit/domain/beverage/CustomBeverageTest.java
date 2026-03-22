package com.hyuns.cafit.domain.beverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.domain.user.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class CustomBeverageTest {

    private User createUser(Long id) {
        User user = new User("test@email.com", "password", "테스트");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void create_팩토리_메서드로_생성할_수_있다() {
        User user = createUser(1L);

        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        assertAll(
                () -> assertThat(beverage.getUser()).isEqualTo(user),
                () -> assertThat(beverage.getName()).isEqualTo("나만의 커피"),
                () -> assertThat(beverage.getCategory()).isEqualTo(BeverageCategory.AMERICANO),
                () -> assertThat(beverage.getVolumeMl()).isEqualTo(400),
                () -> assertThat(beverage.getCaffeineMg()).isEqualTo(200.0)
        );
    }

    @Test
    void update로_이름_용량_카페인을_변경할_수_있다() {
        User user = createUser(1L);
        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        beverage.update("수정된 커피", 500, 250.0);

        assertAll(
                () -> assertThat(beverage.getName()).isEqualTo("수정된 커피"),
                () -> assertThat(beverage.getVolumeMl()).isEqualTo(500),
                () -> assertThat(beverage.getCaffeineMg()).isEqualTo(250.0)
        );
    }

    @Test
    void isOwnedBy는_소유자인_경우_true를_반환한다() {
        User user = createUser(1L);
        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        assertThat(beverage.isOwnedBy(user)).isTrue();
    }

    @Test
    void isOwnedBy는_소유자가_아닌_경우_false를_반환한다() {
        User owner = createUser(1L);
        User other = createUser(2L);
        CustomBeverage beverage = CustomBeverage.create(
                owner, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        assertThat(beverage.isOwnedBy(other)).isFalse();
    }

    @Test
    void getDisplayName은_이름과_용량_포맷으로_반환한다() {
        User user = createUser(1L);
        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        assertThat(beverage.getDisplayName()).isEqualTo("나만의 커피 (400ml)");
    }

    @Test
    void getCaffeineMgPer100ml은_100ml당_카페인을_계산한다() {
        User user = createUser(1L);
        CustomBeverage beverage = CustomBeverage.create(
                user, "나만의 커피", BeverageCategory.AMERICANO, 400, 200.0);

        assertThat(beverage.getCaffeineMgPer100ml()).isEqualTo(50.0);
    }
}
