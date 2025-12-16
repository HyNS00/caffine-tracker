package com.hyuns.cafit.errors;

import lombok.Getter;

@Getter
public abstract class CafitException extends RuntimeException {
    private final ErrorMessage errorMessage;

    protected CafitException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }
}
