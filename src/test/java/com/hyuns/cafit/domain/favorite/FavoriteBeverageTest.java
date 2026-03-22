package com.hyuns.cafit.domain.favorite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@SuppressWarnings("NonAsciiCharacters")
class FavoriteBeverageTest {

    private User createUser(Long id) {
        User user = new User("test@email.com", "password", "테스트");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private PresetBeverage createPresetBeverage() {
        return new PresetBeverage("아메리카노", "스타벅스", BeverageCategory.AMERICANO, 355, 150.0);
    }

    private CustomBeverage createCustomBeverage(User user) {
        return CustomBeverage.create(user, "나만의 커피", BeverageCategory.LATTE, 400, 180.0);
    }

    @Test
    void fromPreset으로_프리셋_즐겨찾기를_생성한다() {
        User user = createUser(1L);
        PresetBeverage beverage = createPresetBeverage();

        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, beverage, 1);

        assertAll(
                () -> assertThat(favorite.getUser()).isEqualTo(user),
                () -> assertThat(favorite.getPresetBeverage()).isEqualTo(beverage),
                () -> assertThat(favorite.getCustomBeverage()).isNull(),
                () -> assertThat(favorite.getDisplayOrder()).isEqualTo(1)
        );
    }

    @Test
    void fromCustom으로_커스텀_즐겨찾기를_생성한다() {
        User user = createUser(1L);
        CustomBeverage beverage = createCustomBeverage(user);

        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(user, beverage, 2);

        assertAll(
                () -> assertThat(favorite.getUser()).isEqualTo(user),
                () -> assertThat(favorite.getPresetBeverage()).isNull(),
                () -> assertThat(favorite.getCustomBeverage()).isEqualTo(beverage),
                () -> assertThat(favorite.getDisplayOrder()).isEqualTo(2)
        );
    }

    @Test
    void 프리셋_즐겨찾기는_isPreset이_true이다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, createPresetBeverage(), 1);

        assertAll(
                () -> assertThat(favorite.isPreset()).isTrue(),
                () -> assertThat(favorite.isCustom()).isFalse()
        );
    }

    @Test
    void 커스텀_즐겨찾기는_isCustom이_true이다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(user, createCustomBeverage(user), 1);

        assertAll(
                () -> assertThat(favorite.isPreset()).isFalse(),
                () -> assertThat(favorite.isCustom()).isTrue()
        );
    }

    @Test
    void isOwnedBy는_소유자인_경우_true를_반환한다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, createPresetBeverage(), 1);

        assertThat(favorite.isOwnedBy(user)).isTrue();
    }

    @Test
    void isOwnedBy는_소유자가_아닌_경우_false를_반환한다() {
        User owner = createUser(1L);
        User other = createUser(2L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(owner, createPresetBeverage(), 1);

        assertThat(favorite.isOwnedBy(other)).isFalse();
    }

    @Test
    void updateOrder로_표시_순서를_변경할_수_있다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, createPresetBeverage(), 1);

        favorite.updateOrder(5);

        assertThat(favorite.getDisplayOrder()).isEqualTo(5);
    }

    @Test
    void getBeverageName은_프리셋_음료_이름을_반환한다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, createPresetBeverage(), 1);

        assertThat(favorite.getBeverageName()).isEqualTo("아메리카노");
    }

    @Test
    void getBeverageName은_커스텀_음료_이름을_반환한다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(user, createCustomBeverage(user), 1);

        assertThat(favorite.getBeverageName()).isEqualTo("나만의 커피");
    }

    @Test
    void getCaffeineMg는_프리셋_음료_카페인을_반환한다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromPreset(user, createPresetBeverage(), 1);

        assertThat(favorite.getCaffeineMg()).isEqualTo(150.0);
    }

    @Test
    void getCaffeineMg는_커스텀_음료_카페인을_반환한다() {
        User user = createUser(1L);
        FavoriteBeverage favorite = FavoriteBeverage.fromCustom(user, createCustomBeverage(user), 1);

        assertThat(favorite.getCaffeineMg()).isEqualTo(180.0);
    }
}
