package com.hyuns.cafit.domain.beverage;

import com.hyuns.cafit.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "custom_beverages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomBeverage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BeverageCategory category;

    @Column(nullable = false, name = "volume_ml")
    private int volumeMl;

    @Column(nullable = false, name = "caffeine_mg")
    private double caffeineMg;

    public CustomBeverage(
            User user,
            String name,
            BeverageCategory category,
            int volumeMl,
            double caffeineMg) {
        this.user = user;
        this.name = name;
        this.category = category;
        this.volumeMl = volumeMl;
        this.caffeineMg = caffeineMg;
    }

    public static CustomBeverage create(
            User user,
            String name,
            BeverageCategory category,
            int volumeMl,
            double caffeineMg) {

        return new CustomBeverage(user, name, category, volumeMl, caffeineMg);
    }

    public String getDisplayName() {
        return String.format("%s (%dml)", name, volumeMl);
    }

    public boolean isOwnedBy(User user) {
        return this.user.equals(user);
    }

    public double getCaffeineMgPer100ml() {
        return (caffeineMg / volumeMl) * 100.0;
    }
}
