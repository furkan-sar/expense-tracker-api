package com.expensetracker.api.service;

import com.expensetracker.api.dto.AuthResponse;
import com.expensetracker.api.dto.LoginRequest;
import com.expensetracker.api.dto.RefreshTokenRequest;
import com.expensetracker.api.dto.RegisterRequest;
import com.expensetracker.api.dto.TokenResponse;
import com.expensetracker.api.dto.UserResponse;
import com.expensetracker.api.entity.RefreshToken;
import com.expensetracker.api.entity.User;
import com.expensetracker.api.exception.AuthenticationException;
import com.expensetracker.api.exception.ConflictException;
import com.expensetracker.api.mapper.UserMapper;
import com.expensetracker.api.repository.RefreshTokenRepository;
import com.expensetracker.api.repository.UserRepository;
import com.expensetracker.api.security.JwtService;
import com.expensetracker.api.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ConflictException("Email is already registered.");
        }

        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());

        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new AuthenticationException("Invalid email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        RefreshToken refreshToken = refreshTokenService.create(user);
        return new AuthResponse(
                jwtService.generateAccessToken(user),
                refreshToken.getToken(),
                TOKEN_TYPE,
                jwtService.accessTokenExpiresInSeconds(),
                userMapper.toResponse(user)
        );
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldRefreshToken = refreshTokenService.requireValid(request.refreshToken());
        User user = oldRefreshToken.getUser();
        refreshTokenRepository.delete(oldRefreshToken);
        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return new TokenResponse(
                jwtService.generateAccessToken(user),
                newRefreshToken.getToken(),
                TOKEN_TYPE,
                jwtService.accessTokenExpiresInSeconds()
        );
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.requireValid(request.refreshToken());
        refreshTokenService.revoke(request.refreshToken());
    }
}
