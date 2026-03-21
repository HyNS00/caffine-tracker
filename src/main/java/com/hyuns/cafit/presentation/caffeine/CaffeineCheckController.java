package com.hyuns.cafit.presentation.caffeine;

import com.hyuns.cafit.global.security.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.application.caffeine.dto.CurrentCaffeineResponse;
import com.hyuns.cafit.application.caffeine.dto.DrinkCheckResponse;
import com.hyuns.cafit.application.caffeine.CaffeineCheckFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caffeine")
@RequiredArgsConstructor
public class CaffeineCheckController {
    private final CaffeineCheckFacade caffeineCheckFacade;

    @GetMapping("/status")
    public ResponseEntity<CurrentCaffeineResponse> getCurrentStatus(
            @Login User user
    ) {
        CurrentCaffeineResponse response = caffeineCheckFacade.getCurrentStatus(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check/preset/{beverageId}")
    public ResponseEntity<DrinkCheckResponse> checkPresetBeverage(
            @Login User user,
            @PathVariable Long beverageId
    ) {
        DrinkCheckResponse response = caffeineCheckFacade.checkPresetBeverage(user, beverageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check/custom/{beverageId}")
    public ResponseEntity<DrinkCheckResponse> checkCustomBeverage(
            @Login User user,
            @PathVariable Long beverageId
    ) {
        DrinkCheckResponse response = caffeineCheckFacade.checkCustomBeverage(user, beverageId);
        return ResponseEntity.ok(response);
    }
}
