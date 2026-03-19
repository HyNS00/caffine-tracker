package com.hyuns.cafit.domain.beverage;

public enum BeverageType {
    PRESET,
    CUSTOM;

    public boolean isPreset() {
        return this == PRESET;
    }
}
