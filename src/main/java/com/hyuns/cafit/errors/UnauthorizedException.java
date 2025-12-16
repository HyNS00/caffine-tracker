package com.hyuns.cafit.errors;

public class UnauthorizedException extends CafitException {
    public UnauthorizedException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
