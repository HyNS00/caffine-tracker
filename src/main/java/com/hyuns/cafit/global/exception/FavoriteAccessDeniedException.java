package com.hyuns.cafit.global.exception;

public class FavoriteAccessDeniedException extends RuntimeException {

    public FavoriteAccessDeniedException() {
        super("해당 즐겨찾기에 대한 권한이 없습니다");
    }
}
