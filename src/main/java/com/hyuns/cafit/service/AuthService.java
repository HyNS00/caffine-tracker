package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.UserRepository;
import com.hyuns.cafit.dto.auth.AuthResponse;
import com.hyuns.cafit.dto.auth.LoginRequest;
import com.hyuns.cafit.dto.auth.SignUpRequest;
import com.hyuns.cafit.errors.AuthenticationException;
import com.hyuns.cafit.errors.DuplicateException;
import com.hyuns.cafit.errors.ErrorMessage;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;

    @Transactional
    public AuthResponse signup(SignUpRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateException(ErrorMessage.DUPLICATE_EMAIL);
        }

        User user = new User(
                request.email(),
                request.password(),
                request.name()
        );

        User savedUser = userRepository.save(user);
        return AuthResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new AuthenticationException(ErrorMessage.LOGIN_FAILED));

        if (!user.isPasswordMatch(request.password())) {
            throw new AuthenticationException(ErrorMessage.LOGIN_FAILED);
        }

        return AuthResponse.from(user);
    }
}
