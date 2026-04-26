package com.tracksure_be.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    @Size(max = 100, message = "fullName must be at most 100 characters")
    private String fullName;

    @Pattern(
            regexp = "^$|^[+0-9()\\-\\s]{7,20}$",
            message = "phoneNumber must contain only digits, spaces, +, -, and parentheses"
    )
    private String phoneNumber;

    @Size(max = 300, message = "bio must be at most 300 characters")
    private String bio;

    @Size(max = 2000, message = "profilePic must be at most 2000 characters")
    private String profilePic;
}

