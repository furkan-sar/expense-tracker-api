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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
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
        log.info("service=AuthService action=register start");
        try {
            String normalizedEmail = request.email().trim().toLowerCase();
            if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
                throw new ConflictException("Email is already registered.");
            }

            User user = new User();
            user.setEmail(normalizedEmail);
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user.setFirstName(request.firstName().trim());
            user.setLastName(request.lastName().trim());

            UserResponse response = userMapper.toResponse(userRepository.save(user));
            log.info("service=AuthService action=register success userId={}", response.id());
            return response;
        } catch (RuntimeException ex) {
            log.error("service=AuthService action=register failure", ex);
            throw ex;
        }
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        log.info("service=AuthService action=login start");
        try {
            User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                    .orElseThrow(() -> new AuthenticationException("Invalid email or password."));

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
                throw new AuthenticationException("Invalid email or password.");
            }

            RefreshToken refreshToken = refreshTokenService.create(user);
            AuthResponse response = new AuthResponse(
                    jwtService.generateAccessToken(user),
                    refreshToken.getToken(),
                    TOKEN_TYPE,
                    jwtService.accessTokenExpiresInSeconds(),
                    userMapper.toResponse(user)
            );
            log.info("service=AuthService action=login success userId={}", user.getId());
            return response;
        } catch (RuntimeException ex) {
            log.error("service=AuthService action=login failure", ex);
            throw ex;
        }
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        log.info("service=AuthService action=refresh start");
        try {
            RefreshToken oldRefreshToken = refreshTokenService.requireValid(request.refreshToken());
            User user = oldRefreshToken.getUser();
            refreshTokenRepository.delete(oldRefreshToken);
            RefreshToken newRefreshToken = refreshTokenService.create(user);

            TokenResponse response = new TokenResponse(
                    jwtService.generateAccessToken(user),
                    newRefreshToken.getToken(),
                    TOKEN_TYPE,
                    jwtService.accessTokenExpiresInSeconds()
            );
            log.info("service=AuthService action=refresh success userId={}", user.getId());
            return response;
        } catch (RuntimeException ex) {
            log.error("service=AuthService action=refresh failure", ex);
            throw ex;
        }
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        log.info("service=AuthService action=logout start");
        try {
            refreshTokenService.requireValid(request.refreshToken());
            refreshTokenService.revoke(request.refreshToken());
            log.info("service=AuthService action=logout success");
        } catch (RuntimeException ex) {
            log.error("service=AuthService action=logout failure", ex);
            throw ex;
        }
    }
}
