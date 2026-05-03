package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicResponse {
    private Long userId;
    private String username;
    private String email;
}
