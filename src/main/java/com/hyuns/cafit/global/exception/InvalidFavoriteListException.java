package com.hyuns.cafit.global.exception;

public class InvalidFavoriteListException extends RuntimeException {

    public InvalidFavoriteListException() {
        super("즐겨찾기 목록이 올바르지 않습니다");
    }
}
