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
     * Revokes all refresh tokens for the authenticated user (logout).
     */
    void logout(String refreshToken);
}
