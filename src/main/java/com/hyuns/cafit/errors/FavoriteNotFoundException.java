package com.hyuns.cafit.errors;

public class FavoriteNotFoundException extends RuntimeException {

    public FavoriteNotFoundException() {
        super("즐겨찾기를 찾을 수 없습니다");
    }
}
