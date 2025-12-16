package com.hyuns.cafit.errors;

public class AuthenticationException extends CafitException {
    public AuthenticationException(ErrorMessage errorMessage) {
        super(errorMessage);
    }
}
