package com.hyuns.cafit.errors;

public class CustomBeverageNotFoundException extends RuntimeException {

    public CustomBeverageNotFoundException() {
        super("커스텀 음료를 찾을 수 없습니다");
    }
}
