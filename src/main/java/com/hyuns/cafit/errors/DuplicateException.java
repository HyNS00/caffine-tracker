package com.hyuns.cafit.errors;

public class DuplicateException extends CafitException{
    public DuplicateException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
