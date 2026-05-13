package com.tracksure_be.dto;

import com.tracksure_be.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;   // JWT
    private String refreshToken;  // stored in AuthToken table
    private Long userId;
    private String username;
    private String email;
    private Role role;
}

