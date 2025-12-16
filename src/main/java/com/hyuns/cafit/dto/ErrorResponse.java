package com.hyuns.cafit.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private String code;
    private String message;
    private Map<String,String> fieldErrors;

    public static ErrorResponse of(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ErrorResponse of(String code, String message, Map<String, String> fieldErrors) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .fieldErrors(fieldErrors)
                .build();
    }
}
