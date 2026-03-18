package com.hyuns.cafit.domain.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.global.entity.CreatedAtEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "favoriate_beverages",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "preset_beverage_id"}),
                @UniqueConstraint(columnNames = {"user_id", "custom_beverage_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteBeverage extends CreatedAtEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preset_beverage_id")
    private PresetBeverage presetBeverage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_beverage_id")
    private CustomBeverage customBeverage;

    @Column(nullable = false, name = "display_order")
    private int displayOrder;


    public static FavoriteBeverage fromPreset(User user, PresetBeverage beverage, int order) {
        FavoriteBeverage favorite = new FavoriteBeverage();
        favorite.user = user;
        favorite.presetBeverage = beverage;
        favorite.displayOrder = order;
        return favorite;
    }

    public static FavoriteBeverage fromCustom(User user, CustomBeverage beverage, int order) {
        FavoriteBeverage favorite = new FavoriteBeverage();
        favorite.user = user;
        favorite.customBeverage = beverage;
        favorite.displayOrder = order;
        return favorite;
    }

    public boolean isPreset() {
        return presetBeverage != null;
    }

    public boolean isCustom() {
        return customBeverage != null;
    }

    public boolean isOwnedBy(User user) {
        return this.user.equals(user);
    }

    public void updateOrder(int newOrder) {
        this.displayOrder = newOrder;
    }

    public String getBeverageName() {
        return isPreset() ? presetBeverage.getName() : customBeverage.getName();
    }

    public double getCaffeineMg() {
        return isPreset() ? presetBeverage.getCaffeineMg() : customBeverage.getCaffeineMg();
    }
}
