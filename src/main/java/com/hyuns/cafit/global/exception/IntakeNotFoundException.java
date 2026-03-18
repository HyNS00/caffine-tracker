package com.hyuns.cafit.global.exception;

public class IntakeNotFoundException extends RuntimeException {

    public IntakeNotFoundException() {
        super("섭취 기록을 찾을 수 없습니다");
    }
}
