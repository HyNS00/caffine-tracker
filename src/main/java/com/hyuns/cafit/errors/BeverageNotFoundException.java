package com.hyuns.cafit.errors;

public class BeverageNotFoundException extends RuntimeException {

    public BeverageNotFoundException() {
        super("음료를 찾을 수 없습니다");
    }
}
