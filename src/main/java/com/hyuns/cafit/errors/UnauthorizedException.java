package com.hyuns.cafit.errors;

public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException() {
        super("로그인이 필요합니다");
    }
}
