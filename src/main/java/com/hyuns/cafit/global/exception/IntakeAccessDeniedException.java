package com.hyuns.cafit.global.exception;

public class IntakeAccessDeniedException extends RuntimeException {

    public IntakeAccessDeniedException() {
        super("해당 기록에 대한 권한이 없습니다");
    }
}
