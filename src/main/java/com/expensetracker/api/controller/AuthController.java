package com.expensetracker.api.controller;

import com.expensetracker.api.dto.AuthResponse;
import com.expensetracker.api.dto.LoginRequest;
import com.expensetracker.api.dto.RefreshTokenRequest;
import com.expensetracker.api.dto.RegisterRequest;
import com.expensetracker.api.dto.SuccessResponse;
import com.expensetracker.api.dto.TokenResponse;
import com.expensetracker.api.dto.UserResponse;
import com.expensetracker.api.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refresh(request);
    }

    @PostMapping("/logout")
    public SuccessResponse logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request);
        return new SuccessResponse(true);
    }
}
