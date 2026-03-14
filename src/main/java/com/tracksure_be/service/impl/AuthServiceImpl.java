package com.tracksure_be.service.impl;

import com.tracksure_be.dto.LoginRequest;
import com.tracksure_be.dto.LoginResponse;
import com.tracksure_be.dto.SignupRequest;
import com.tracksure_be.entity.RefreshToken;
import com.tracksure_be.entity.User;
import com.tracksure_be.exception.EmailAlreadyExistsException;
import com.tracksure_be.exception.InvalidTokenException;
import com.tracksure_be.repository.RefreshTokenRepository;
import com.tracksure_be.repository.UserRepository;
import com.tracksure_be.security.JwtProvider;
import com.tracksure_be.security.UserPrincipal;
import com.tracksure_be.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ── Register ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse register(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                    "An account with email '" + request.getEmail() + "' already exists.");
        }

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setCreatedAt(Instant.now());

        user = userRepository.save(user);

        UserPrincipal principal = UserPrincipal.of(user);
        String accessToken = jwtProvider.generateToken(principal);
        String refreshToken = createRefreshToken(user);

        return buildResponse(accessToken, refreshToken, user);
    }

    // ── Login ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        User user = userRepository.findByUsername(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found in database"));

        String accessToken = jwtProvider.generateToken(principal);
        String refreshToken = createRefreshToken(user);

        return buildResponse(accessToken, refreshToken, user);
    }

    // ── Refresh ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LoginResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found."));

        if (stored.isRevoked()) {
            throw new InvalidTokenException("Refresh token has been revoked.");
        }

        if (stored.getExpiresAt().isBefore(Instant.now())) {
            stored.setRevoked(true);
            refreshTokenRepository.save(stored);
            throw new InvalidTokenException("Refresh token has expired. Please log in again.");
        }

        User user = stored.getUser();
        UserPrincipal principal = UserPrincipal.of(user);
        String newAccessToken = jwtProvider.generateToken(principal);

        // Rotate the refresh token
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        String newRefreshToken = createRefreshToken(user);

        return buildResponse(newAccessToken, newRefreshToken, user);
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    refreshTokenRepository.save(rt);
                });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String createRefreshToken(User user) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .revoked(false)
                .build();
        return refreshTokenRepository.save(refreshToken).getToken();
    }

    private LoginResponse buildResponse(String accessToken, String refreshToken, User user) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getUserId(),
                user.getUsername(),
                user.getEmail());
    }
}
