package com.hyuns.cafit.service;

import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.domain.user.repository.UserRepository;
import com.hyuns.cafit.dto.auth.AuthResponse;
import com.hyuns.cafit.dto.auth.LoginRequest;
import com.hyuns.cafit.dto.auth.SignUpRequest;
import com.hyuns.cafit.errors.DuplicateEmailException;
import com.hyuns.cafit.errors.LoginFailedException;

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
