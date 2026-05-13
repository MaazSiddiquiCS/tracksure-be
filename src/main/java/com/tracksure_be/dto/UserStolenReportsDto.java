package com.tracksure_be.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for stolen reports grouped by user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserStolenReportsDto {
    
    private Long userId;
    private String username;
    private String email;
    private List<StolenDeviceResponse> stolenReports;
}
