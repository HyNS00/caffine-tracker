package com.hyuns.cafit.application.auth.dto;

import com.hyuns.cafit.domain.user.User;

public record AuthResponse(
        Long userId,
        String email,
        String name
) {
    public static AuthResponse from(User user) {
        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                user.getName()
        );
    }
}
