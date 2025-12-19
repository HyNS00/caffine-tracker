package com.hyuns.cafit.domain.favorite;

import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoriate_beverages",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "preset_beverage_id"}),
                @UniqueConstraint(columnNames = {"user_id", "custom_beverage_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FavoriteBeverage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;


    // Preset용 생성
    public static FavoriteBeverage fromPreset(
            User user,
            PresetBeverage beverage,
            int order,
            LocalDateTime now
    ) {
        FavoriteBeverage favorite = new FavoriteBeverage();
        favorite.user = user;
        favorite.presetBeverage = beverage;
        favorite.displayOrder = order;
        favorite.createdAt = now;
        return favorite;
    }
    // 커스텀용
    public static FavoriteBeverage fromCustom(
            User user,
            CustomBeverage beverage,
            int order,
            LocalDateTime now
    ) {
        FavoriteBeverage favorite = new FavoriteBeverage();
        favorite.user = user;
        favorite.customBeverage = beverage;
        favorite.displayOrder = order;
        favorite.createdAt = now;
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
