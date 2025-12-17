package com.hyuns.cafit.controller;

import com.hyuns.cafit.auth.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.caffeine.CurrentCaffeineResponse;
import com.hyuns.cafit.dto.caffeine.DrinkCheckResponse;
import com.hyuns.cafit.service.CaffeineCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caffeine")
@RequiredArgsConstructor
public class CaffeineCheckController {
    private final CaffeineCheckService caffeineCheckService;

    @GetMapping("/status")
    public ResponseEntity<CurrentCaffeineResponse> getCurrentStatus(
            @Login User user
    ) {
        CurrentCaffeineResponse response = caffeineCheckService.getCurrentStatus(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check/preset/{beverageId}")
    public ResponseEntity<DrinkCheckResponse> checkPresetBeverage(
            @Login User user,
            @PathVariable Long beverageId
    ) {
        DrinkCheckResponse response = caffeineCheckService.checkPresetBeverage(user, beverageId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check/custom/{beverageId}")
    public ResponseEntity<DrinkCheckResponse> checkCustomBeverage(
            @Login User user,
            @PathVariable Long beverageId
    ) {
        DrinkCheckResponse response = caffeineCheckService.checkCustomBeverage(user, beverageId);
        return ResponseEntity.ok(response);
    }
}
