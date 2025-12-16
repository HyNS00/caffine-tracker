package com.hyuns.cafit.controller;

import com.hyuns.cafit.auth.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.intake.CaffeineIntakeCreateRequest;
import com.hyuns.cafit.dto.intake.CaffeineIntakeResponse;
import com.hyuns.cafit.service.CaffeineIntakeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/intakes")
public class CaffeineIntakeController {
    private final CaffeineIntakeService caffeineIntakeService;

    @PostMapping("/preset/{beverageId}")
    public ResponseEntity<CaffeineIntakeResponse> recordPresetIntake(
            @PathVariable Long beverageId,
            @Login User user,
            @Valid @RequestBody CaffeineIntakeCreateRequest request
    ) {
        CaffeineIntakeResponse response = caffeineIntakeService.recordPresetIntake(user, beverageId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/custom/{beverageId}")
    public ResponseEntity<CaffeineIntakeResponse> recordCustomIntake(
            @PathVariable Long beverageId,
            @Login User user,
            @Valid @RequestBody CaffeineIntakeCreateRequest request
    ) {
        CaffeineIntakeResponse response = caffeineIntakeService.recordCustomIntake(user, beverageId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/today")
    public ResponseEntity<List<CaffeineIntakeResponse>> getTodayIntakes(
            @Login User user
    ) {
        List<CaffeineIntakeResponse> responses = caffeineIntakeService.getTodayIntakes(user);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{intakeId}")
    public ResponseEntity<Void> deleteIntake(
            @PathVariable Long intakeId,
            @Login User user
    ) {
        caffeineIntakeService.deleteIntake(intakeId, user);
        return ResponseEntity.noContent().build();
    }

}
