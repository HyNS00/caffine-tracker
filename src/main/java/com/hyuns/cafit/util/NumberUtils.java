package com.hyuns.cafit.util;

public class NumberUtils {
    public static double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private NumberUtils() {}
}
