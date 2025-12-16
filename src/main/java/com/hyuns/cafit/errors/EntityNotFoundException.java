package com.hyuns.cafit.errors;

public class EntityNotFoundException extends CafitException {
    public EntityNotFoundException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
