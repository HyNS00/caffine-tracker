package com.hyuns.cafit.application.auth;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import com.hyuns.cafit.application.auth.dto.AuthResponse;
import com.hyuns.cafit.application.auth.dto.LoginRequest;
import com.hyuns.cafit.application.auth.dto.SignUpRequest;
import com.hyuns.cafit.global.exception.DuplicateEmailException;
import com.hyuns.cafit.global.exception.LoginFailedException;

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
            throw new DuplicateEmailException();
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
                .orElseThrow(LoginFailedException::new);

        if (!user.isPasswordMatch(request.password())) {
            throw new LoginFailedException();
        }

        return AuthResponse.from(user);
    }
}
