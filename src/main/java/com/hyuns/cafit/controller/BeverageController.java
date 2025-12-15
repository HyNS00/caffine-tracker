package com.hyuns.cafit.controller;

import com.hyuns.cafit.dto.beverage.BeverageCategoryResponse;
import com.hyuns.cafit.dto.beverage.PresetBeverageResponse;
import com.hyuns.cafit.service.PresetBeverageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beverages")
@RequiredArgsConstructor
public class BeverageController {
    private final PresetBeverageService presetBeverageService;

    @GetMapping
    public ResponseEntity<List<PresetBeverageResponse>> getBeverages(
            @RequestParam(required = false) String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return ResponseEntity.ok(presetBeverageService.getAllBeverages());
        }
        return ResponseEntity.ok(presetBeverageService.searchBeverages(keyword));
    }

    @GetMapping("/categories")
    public ResponseEntity<List<BeverageCategoryResponse>> getCategories() {
        return ResponseEntity.ok(presetBeverageService.getAllCategories());
    }

}
