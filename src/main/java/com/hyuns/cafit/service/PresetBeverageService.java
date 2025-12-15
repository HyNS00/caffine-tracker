package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.beverage.BeverageCategory;
import com.hyuns.cafit.domain.beverage.PresetBeverageRepository;
import com.hyuns.cafit.dto.beverage.BeverageCategoryResponse;
import com.hyuns.cafit.dto.beverage.PresetBeverageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PresetBeverageService {
    private final PresetBeverageRepository presetBeverageRepository;

    public List<PresetBeverageResponse> getAllBeverages() {
        return presetBeverageRepository.findAll()
                .stream()
                .map(PresetBeverageResponse::from)
                .toList();
    }

    public List<PresetBeverageResponse> searchBeverages(String keyword) {
        return presetBeverageRepository.searchPresetBeverageByKeyword(keyword)
                .stream()
                .map(PresetBeverageResponse::from)
                .toList();
    }

    public List<BeverageCategoryResponse> getAllCategories() {
        return Arrays.stream(BeverageCategory.values())
                .map(BeverageCategoryResponse::from)
                .toList();
    }
}
