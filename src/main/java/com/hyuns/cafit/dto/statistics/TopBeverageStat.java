package com.hyuns.cafit.dto.statistics;

public record TopBeverageStat(
        String beverageName,
        String brandName,
        int volumeMl,
        long count
) {}
