package com.hyuns.cafit.application.statistics.dto;

public record TopBeverageStat(
        String beverageName,
        String brandName,
        int volumeMl,
        long count
) {}
