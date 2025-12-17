package com.hyuns.cafit.controller;

import com.hyuns.cafit.auth.Login;
import com.hyuns.cafit.domain.user.User;
import com.hyuns.cafit.dto.auth.AuthResponse;
import com.hyuns.cafit.dto.auth.LoginRequest;
import com.hyuns.cafit.dto.auth.SignUpRequest;
import com.hyuns.cafit.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignUpRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session
    ) {
        AuthResponse response = authService.login(request);

        session.setAttribute("userId", response.userId());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@Login User user) {
        return ResponseEntity.ok(AuthResponse.from(user));
    }
}
