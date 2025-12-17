package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.intake.CaffeineIntake;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class CaffeineDecayCalculator {
    public double calculateRemaining(double initialMg, double hoursElapsed, double halfLife) {
        if (hoursElapsed <= 0) {
            return initialMg;
        }
        return initialMg * Math.pow(0.5, hoursElapsed / halfLife);
    }

    public double caffeineLevelAt(List<CaffeineIntake> intakes, LocalDateTime targetTime, double halfLife) {
        double total = 0;

        for (CaffeineIntake intake : intakes) {
            double hoursElapsed = ChronoUnit.MINUTES.between(intake.getConsumedAt(), targetTime) / 60.0;
            total += calculateRemaining(intake.getCaffeineMg(), hoursElapsed, halfLife);
        }

        return total;
    }
}
