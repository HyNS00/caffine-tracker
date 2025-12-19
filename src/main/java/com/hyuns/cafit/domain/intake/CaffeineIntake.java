package com.hyuns.cafit.domain.intake;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.beverage.BeverageType;
import com.hyuns.cafit.domain.beverage.CustomBeverage;
import com.hyuns.cafit.domain.beverage.PresetBeverage;
import com.hyuns.cafit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "caffeine_intakes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaffeineIntake {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100, name = "beverage_name")
    private String beverageName;

    @Column(length = 50, name = "brand_name")
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BeverageCategory category;

    @Column(nullable = false, name = "volume_ml")
    private int volumeMl;

    @Column(nullable = false, name = "caffeine_mg")
    private double caffeineMg;

    @Column(nullable = false, name = "consumed_at")
    private LocalDateTime consumedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", length = 10)
    private BeverageType sourceType;

    @Column(name = "source_beverage_id")
    private Long sourceBeverageId;

    private CaffeineIntake(
            User user,
            String beverageName,
            String brandName,
            BeverageCategory category,
            int volumeMl,
            double caffeineMg,
            LocalDateTime consumedAt,
            BeverageType sourceType,
            Long sourceBeverageId
    ) {
        this.user = user;
        this.beverageName = beverageName;
        this.brandName = brandName;
        this.category = category;
        this.volumeMl = volumeMl;
        this.caffeineMg = caffeineMg;
        this.consumedAt = consumedAt;
        this.sourceType = sourceType;
        this.sourceBeverageId = sourceBeverageId;
    }

    public static CaffeineIntake fromPreset(User user, PresetBeverage beverage, LocalDateTime consumedAt) {
        return new CaffeineIntake(
                user,
                beverage.getName(),
                beverage.getBrandName(),
                beverage.getCategory(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                consumedAt,
                BeverageType.PRESET,
                beverage.getId()
        );
    }

    public static CaffeineIntake fromCustom(User user, CustomBeverage beverage, LocalDateTime consumedAt) {
        return new CaffeineIntake(
                user,
                beverage.getName(),
                null,
                beverage.getCategory(),
                beverage.getVolumeMl(),
                beverage.getCaffeineMg(),
                consumedAt,
                BeverageType.CUSTOM,
                beverage.getId()
        );
    }

    public String getDisplayName() {
        if (brandName != null) {
            return String.format("%s(%s) %dml - %.0fmg", beverageName, brandName, volumeMl, caffeineMg);
        }
        return String.format("%s %dml - %.0fmg", beverageName, volumeMl, caffeineMg);
    }

    public boolean isOwnedBy(User user) {
        return this.user.equals(user);
    }

}
