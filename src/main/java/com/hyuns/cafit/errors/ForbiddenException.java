package com.hyuns.cafit.errors;

public class ForbiddenException extends CafitException {
    public ForbiddenException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
