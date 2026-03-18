package com.hyuns.cafit.global.exception;

public class DuplicateFavoriteException extends RuntimeException {

    public DuplicateFavoriteException() {
        super("이미 즐겨찾기에 추가된 음료입니다");
    }
}
