package com.tracksure_be.service;

import com.tracksure_be.dto.LoginRequest;
import com.tracksure_be.dto.LoginResponse;
import com.tracksure_be.dto.SignupRequest;

public interface AuthService {

    /**
     * Registers a new user and returns auth tokens.
     */
    LoginResponse register(SignupRequest request);

    /**
     * Authenticates an existing user and returns auth tokens.
     */
    LoginResponse login(LoginRequest request);

    /**
     * Generates a new access token from a valid refresh token.
     */
    LoginResponse refresh(String refreshToken);

    /**
     * Revokes the provided refresh token (logout from current session).
     * To revoke all tokens for a user (logout from all devices), use
     * {@link com.tracksure_be.repository.RefreshTokenRepository#revokeAllByUser}.
     */
    void logout(String refreshToken);
}
