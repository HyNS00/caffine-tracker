package com.hyuns.cafit.domain.beverage;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "preset_beverages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PresetBeverage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50, name = "brand_name")
    private String brandName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BeverageCategory category;

    @Column(nullable = false, name = "volume_ml")
    private int volumeMl;

    @Column(nullable = false, name = "caffeine_mg")
    private double caffeineMg;

    public PresetBeverage(
            String name,
            String brandName,
            BeverageCategory category,
            int volumeMl,
            double caffeineMg) {
        this.name = name;
        this.brandName = brandName;
        this.category = category;
        this.volumeMl = volumeMl;
        this.caffeineMg = caffeineMg;
    }

    public String getDisplayName() {
        return String.format("%s %s (%dml)", brandName, name, volumeMl);
    }

    public double getCaffeineMgPer100ml() {
        return (caffeineMg / volumeMl) * 100.0;
    }

    public double estimateCaffeineForVolume(int customVolumeMl) {
        return getCaffeineMgPer100ml() * customVolumeMl / 100.0;
    }
}
