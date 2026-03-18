package com.chatbot.chatbot.service;

import com.chatbot.chatbot.dto.Request.AuthRequest;
import com.chatbot.chatbot.dto.Request.RegisterRequest;
import com.chatbot.chatbot.dto.Response.AuthResponse;
import com.chatbot.chatbot.jwt.JwtUtil;
import com.chatbot.chatbot.model.RefreshToken;
import com.chatbot.chatbot.model.enumList.Role;
import com.chatbot.chatbot.model.User;
import com.chatbot.chatbot.repository.RefreshTokenRepository;
import com.chatbot.chatbot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    // ── Register ─────────────────────────────────────────
    public AuthResponse register(RegisterRequest request) {

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Build and save user
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        // Generate tokens
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ── Login ────────────────────────────────────────────
    @Transactional
    public AuthResponse login(AuthRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        revokeAllUserTokens(user);

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // ── Helper ───────────────────────────────────────────
    private void revokeAllUserTokens(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // ── Refresh Token ────────────────────────────────────
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {

        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        // Check if revoked
        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token has been revoked");
        }

        // Check if expired
        if (token.getExpiryDate().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token has expired");
        }

        User user = token.getUser();

        // Revoke old token & generate new ones
        revokeAllUserTokens(user);
        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // ── Logout ───────────────────────────────────────────
    @Transactional
    public void logout(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    // ── Helper methods ───────────────────────────────────
    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshExpiration))
                .revoked(false)
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

}