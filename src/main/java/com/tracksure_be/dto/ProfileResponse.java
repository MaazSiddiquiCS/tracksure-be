package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long profileId;
    private String fullName;
    private String phoneNumber;
    private String bio;
    private String profilePic;
    private Long userId;
}

