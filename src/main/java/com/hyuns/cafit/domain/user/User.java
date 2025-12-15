package com.hyuns.cafit.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;
    /**
     * 일일 카페인 섭취 권장량
     */
    @Column(nullable = false, name = "daily_caffeine_limit")
    private int dailyCaffeineLimit;
    /**
     * 카페인 반감기
     * 단위는 시간
     */
    @Column(nullable = false, name = "caffeine_half_life")
    private double caffeineHalfLife;

    /**
     * 목표 취침시간
     */
    @Column(nullable = false, name = "bed_time")
    private LocalTime bedTime;

    /**
     * 취침 시 목표 카페인 잔량 (mg)
     * 기본값: 50mg
     */
    @Column(nullable = false, name = "target_sleep_caffeine")
    private double targetSleepCaffeine;

    public User(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.dailyCaffeineLimit = 400;
        this.caffeineHalfLife = 5.0;
        this.bedTime = LocalTime.of(23, 0);
        this.targetSleepCaffeine = 50.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
