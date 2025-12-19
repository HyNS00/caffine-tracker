package com.hyuns.cafit.errors;

public class BadRequestException extends CafitException{
    public BadRequestException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
