package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileRequest {
    private String fullName;
    private String phoneNumber;
    private String bio;
    private String profilePic;
    private Long userId;
}

