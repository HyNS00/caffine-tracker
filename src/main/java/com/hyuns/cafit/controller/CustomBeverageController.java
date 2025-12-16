package com.hyuns.cafit.controller;

import com.hyuns.cafit.auth.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.beverage.CustomBeverageCreateRequest;
import com.hyuns.cafit.dto.beverage.CustomBeverageResponse;
import com.hyuns.cafit.dto.beverage.CustomBeverageUpdateRequest;
import com.hyuns.cafit.service.CustomBeverageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beverages/custom")
@RequiredArgsConstructor
public class CustomBeverageController {
    private final CustomBeverageService customBeverageService;

    @PostMapping
    public ResponseEntity<CustomBeverageResponse> createCustomBeverage(
            @Login User user,
            @Valid @RequestBody CustomBeverageCreateRequest request
    ) {
        CustomBeverageResponse response = customBeverageService.createCustomBeverage(user, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<CustomBeverageResponse>> getMyCustomBeverages(
            @Login User user
    ) {
        List<CustomBeverageResponse> responses = customBeverageService.getMyCustomBeverages(user);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{beverageId}")
    public ResponseEntity<CustomBeverageResponse> updateCustomBeverage(
            @PathVariable Long beverageId,
            @Login User user,
            @Valid @RequestBody CustomBeverageUpdateRequest request
    ) {
        CustomBeverageResponse response = customBeverageService.updateCustomBeverage(
                beverageId, user, request
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{beverageId}")
    public ResponseEntity<Void> deleteCustomBeverage(
            @PathVariable Long beverageId,
            @Login User user
    ) {
        customBeverageService.deleteCustomBeverage(beverageId, user);
        return ResponseEntity.noContent().build();
    }
}
