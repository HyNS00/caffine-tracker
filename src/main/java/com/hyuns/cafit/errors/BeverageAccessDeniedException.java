package com.hyuns.cafit.errors;

public class BeverageAccessDeniedException extends RuntimeException {

    public BeverageAccessDeniedException() {
        super("해당 음료에 대한 권한이 없습니다");
    }
}
